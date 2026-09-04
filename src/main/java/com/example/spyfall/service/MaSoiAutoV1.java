package com.example.spyfall.service;

import com.example.spyfall.common.AutoSelectRequest;
import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.NightActionDto;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.spyfall.common.NightActionType.KILL;
import static com.example.spyfall.common.NightActionType.RECRUIT;
import static com.example.spyfall.common.NightActionType.SOI;

@Service
public class MaSoiAutoV1 {
    private static final Logger log = LoggerFactory.getLogger(MaSoiAutoV1.class);

    private static final String STATE_DESTINATION = "/ms/autoV1/state";
//    private static final Set<Integer> HIDDEN_CONNECT_SKILLS = Set.of(5);
    private static final Set<Integer> HIDDEN_ID = Set.of(37, 40, 41);
    private static final Set<Integer> ACTION_COUNTDOWN_ROLE_IDS = Set.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 20); // lien quan den check endgame

    // Vấn đề 7: tách list countdown riêng cho role Silent (38).
    private static final Set<Integer> SILENT_COUNTDOWN_ROLE_IDS = Set.of(38);
    // Vấn đề 11: các role trong list này không được chọn chính bản thân mình trong bảng đêm.
    private static final Set<Integer> SELF_SELECT_DISABLED_ROLE_IDS = Set.of(32, 34, 38, 42, 43, 45);
    // Giá trị đặc biệt để đánh dấu người chơi chọn bỏ qua thay vì chọn ai
    private static final String SKIP_TARGET = "__SKIP__";
    private final List<String> reasonKills = List.of(" bị úp mặt vào tường", " bị đuổi ra khỏi làng", " bị nhốt trong nhà vệ sinh");
    private String reasonKill;

    private final MaSoiService maSoiService;
    private final SimpMessagingTemplate messagingTemplate;
    private final Object lock = new Object();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "masoi-auto-v1");
        thread.setDaemon(true);
        return thread;
    });

    @Getter
    private int dayDurationSeconds = 90;
    @Getter
    private int nightDurationSeconds = 120;
    @Getter
    private int voteDurationSeconds = 45;
    @Getter
    private int dayRemainingSeconds = 60;
    @Getter
    private int nightRemainingSeconds = 120;
    private int nightRemainingSecondRandom = 1000;
    @Getter
    private int voteRemainingSeconds = 45;
    @Getter
    private int selectionCountdownSeconds = 60;
    @Getter
    private int selectionSeconds = 40;
    private int selectionCountdownSecondRandom = 1000;
    @Getter
    private int silentCountdownSeconds = 60;
    @Getter
    private int silentSeconds = 40;
    private int silentCountdownSecondRandom = 1000;

    private int toughGuyRemainingSeconds = 60;

    private final Map<String, String> daySelections = new LinkedHashMap<>();
    private final Map<String, NightActionDto> nightSelections = new LinkedHashMap<>();
    // Map deviceId → "yes" hoặc "no" cho bảng vote
    private final Map<String, String> voteSelections = new LinkedHashMap<>();

    private boolean dayTimeoutSent;
    private boolean nightTimeoutSent;
    private boolean voteTimeoutSent;
    @Getter
    @Setter
    private boolean isNeedVote = true;

    // Cờ hiển thị bảng ngày/đêm/vote cho toàn bộ người chơi – admin bật/tắt qua run.html
    @Getter
    private boolean showDayBoard = false;
    @Getter
    private boolean showNightBoard = false;
    @Getter
    private boolean showVoteBoard = false;
    @Getter
    private boolean showToughGuy = false;
    private String killDayDevice = null;

    private Integer randomVictim = null;

    private boolean nextDayBlockHandle = false;

    private static final String IMAGE_PATH = "/msAutoV1.png";

    public String getImage() {
        return IMAGE_PATH;
    }

    String toughGuySelection = "";

    @Autowired
    public MaSoiAutoV1(MaSoiService maSoiService, SimpMessagingTemplate messagingTemplate) {
        this.maSoiService = maSoiService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    void startTicker() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                tick();
            } catch (Exception ex) {
                // Nếu tick ném lỗi thì ScheduledExecutor sẽ dừng luôn các lần chạy sau; bắt lỗi để ticker không chết ngầm.
                log.error("AutoV1 ticker failed on tick, keeping scheduler alive", ex);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @PreDestroy
    void stopTicker() {
        scheduler.shutdownNow();
    }

    public Map<String, String> getMemers() {
        synchronized (lock) {
            // FE chỉ cần deviceId -> tên để vẽ danh sách người chơi.
            Map<String, String> members = new LinkedHashMap<>();
            for (DataMember player : maSoiService.getPls()) {
                if (!ObjectUtils.isEmpty(player.getIpData())) {
                    members.put(player.getIpData(), player.getNameMember() != null ? player.getNameMember() : "Ẩn danh");
                }
            }
            return members;
        }
    }

    public Map<String, Object> getState(String viewerDeviceId) {
        synchronized (lock) {
            return buildState(viewerDeviceId);
        }
    }

    public Map<String, Object> updateDurations(int daySeconds, int nightSeconds, int voteSeconds, int selectionSeconds, int silentSeconds, String viewerDeviceId) {
        synchronized (lock) {
            dayDurationSeconds = Math.max(1, daySeconds);
            nightDurationSeconds = Math.max(1, nightSeconds);
            voteDurationSeconds = Math.max(1, voteSeconds);
            this.selectionSeconds = Math.max(1, selectionSeconds);
            this.silentSeconds = Math.max(1, silentSeconds);
            dayRemainingSeconds = dayDurationSeconds;
            nightRemainingSeconds = nightDurationSeconds;
            voteRemainingSeconds = voteDurationSeconds;
            selectionCountdownSeconds = selectionSeconds;
            silentCountdownSeconds = silentSeconds;
            dayTimeoutSent = false;
            nightTimeoutSent = false;
            voteTimeoutSent = false;
            broadcastStateLocked();
            return buildState(viewerDeviceId);
        }
    }

    // Admin bật/tắt hiển thị bảng ngày; broadcast ngay để tất cả người chơi thấy
    public Map<String, Object> setShowDayBoard(boolean show) {
        synchronized (lock) {
            showDayBoard = show;
            if (show) {
                // Reset đồng hồ về mặc định khi bật lại để đếm ngược từ đầu
                dayRemainingSeconds = dayDurationSeconds;
                dayTimeoutSent = false;
            }
            broadcastStateLocked();
            return buildState(null);
        }
    }

    // Admin bật/tắt hiển thị bảng đêm; broadcast ngay để tất cả người chơi thấy
    public Map<String, Object> setShowNightBoard(boolean show) {
        synchronized (lock) {
            showNightBoard = show;
            if (show) {
                // Reset đồng hồ về mặc định khi bật lại để đếm ngược từ đầu
                randomVictim = null;
                nightRemainingSeconds = nightDurationSeconds;
                selectionCountdownSeconds = selectionSeconds;
                silentCountdownSeconds = silentSeconds;
                silentCountdownSecondRandom = 1000;
                nightRemainingSecondRandom = 1000;
                selectionCountdownSecondRandom = 1000;
                nightTimeoutSent = false;
                maSoiService.NOT_SHOW_DAY.clear();
            }
            broadcastStateLocked();
            if (show) {
                // Gửi tín hiệu refresh cho toàn bộ UI đang subscribe khi bật bảng đêm.
                broadcastRefreshUiLocked("night-board-enabled");
            }
            return buildState(null);
        }
    }

    // Admin bật/tắt hiển thị bảng vote Yes/No; xóa sạch vote cũ khi bật lại
    public Map<String, Object> setShowVoteBoard(boolean show) {
        synchronized (lock) {
            showVoteBoard = show;
            if (show) {
                voteRemainingSeconds = voteDurationSeconds;
                voteTimeoutSent = false;
                voteSelections.clear(); // Reset vote mỗi lần bật mới
            }
            broadcastStateLocked();
            return buildState(null);
        }
    }

    // Người chơi gửi vote Yes/No; toggle nếu vote lại cùng đáp án
    public Map<String, Object> submitVoteSelection(String actorDeviceId, String answer) {
        synchronized (lock) {
            DataMember actor = findPlayer(actorDeviceId).orElse(null);
            if (actor == null || actor.isDead()) {
                return errorState("Người chơi không hợp lệ");
            }
            // Chuẩn hoá đáp án: chỉ nhận "yes" hoặc "no"
            String normalized = "yes".equalsIgnoreCase(answer) ? "yes" : "no";
            if (normalized.equals(voteSelections.get(actorDeviceId))) {
                // Toggle off: bỏ vote
                voteSelections.remove(actorDeviceId);
            } else {
                voteSelections.put(actorDeviceId, normalized);
                if (allVoteSelected() && voteRemainingSeconds > 5) {
                    voteRemainingSeconds = 5;
                }
            }
            broadcastStateLocked();
            return buildState(actorDeviceId);
        }
    }

    public Map<String, Object> selectDayTarget(AutoSelectRequest request) {
        synchronized (lock) {
            DataMember actor = findPlayer(request.getActorDeviceId()).orElse(null);
            DataMember target = findPlayer(request.getTargetDeviceId()).orElse(null);
            if (actor == null || target == null || actor.isDead() || target.isDead()) {
                return errorState("Target không hợp lệ");
            }
            if (Objects.equals(daySelections.get(actor.getIpData()), target.getIpData())) {
                daySelections.remove(actor.getIpData());
                broadcastStateLocked();
                return buildState(actor.getIpData());
            }
            daySelections.put(actor.getIpData(), target.getIpData());
            if (allDaySelected() && dayRemainingSeconds > 5) {
                dayRemainingSeconds = 5;
            }
            broadcastStateLocked();
            return buildState(actor.getIpData());
        }
    }

    public Map<String, Object> selectToughGuyTarget(AutoSelectRequest request) {
        synchronized (lock) {
            DataMember actor = findPlayer(request.getActorDeviceId()).orElse(null);
            DataMember target = findPlayer(request.getTargetDeviceId()).orElse(null);
            if (actor == null || target == null || actor.isDead() || target.isDead()) {
                return errorState("Target không hợp lệ");
            }
            if (Objects.equals(toughGuySelection, target.getIpData())) {
                toughGuySelection = "";
                broadcastStateLocked();
                return buildState(actor.getIpData());
            }
            toughGuySelection = target.getIpData();
            if (toughGuyRemainingSeconds > 10) {
                toughGuyRemainingSeconds = 10;
            }
            broadcastStateLocked();
            return buildState(actor.getIpData());
        }
    }

    // Người chơi chọn "bỏ qua" thay vì vote ai đó; nếu đã skip thì toggle về chưa chọn
    public Map<String, Object> skipDaySelection(String actorDeviceId) {
        synchronized (lock) {
            DataMember actor = findPlayer(actorDeviceId).orElse(null);
            if (actor == null || actor.isDead()) {
                return errorState("Người chơi không hợp lệ");
            }
            if (SKIP_TARGET.equals(daySelections.get(actor.getIpData()))) {
                // Đã skip rồi → bỏ skip (toggle)
                daySelections.remove(actor.getIpData());
            } else {
                // Đánh dấu bỏ qua; xóa vote cũ nếu có
                daySelections.put(actor.getIpData(), SKIP_TARGET);
                if (allDaySelected() && dayRemainingSeconds > 5) {
                    dayRemainingSeconds = 5;
                }
            }
            broadcastStateLocked();
            return buildState(actor.getIpData());
        }
    }

    public Map<String, Object> selectNightTarget(AutoSelectRequest request) {
        synchronized (lock) {
            DataMember actor = findPlayer(request.getActorDeviceId()).orElse(null);
            String targetDeviceId = request.getTargetDeviceId();
            boolean isSkip = SKIP_TARGET.equals(targetDeviceId);
            DataMember target = isSkip ? null : findPlayer(targetDeviceId).orElse(null);
            if (actor == null || actor.isDead() || (!isSkip && (target == null || target.isDead()))) {
                return errorState("Target không hợp lệ");
            }
            if (!hasNightAction(actor)) {
                return errorState("Nhân vật này không có chức năng đêm");
            }

            NightActionDto action = new NightActionDto();
            action.setDeviceId(actor.getIpData());
            action.setRoleId(actor.getId());
            action.setRoleName(removeHtml(actor.getRole()));
            action.setColType(normalizeActionType(request.getColType()));
            if (RECRUIT.code.equals(action.getColType())
                    && (!isWolfNightActor(actor.getId()) || !maSoiService.isShowSoiNguyen())) {
                return errorState("Sói hiện không thể nguyền");
            }
            action.setConnValue(request.getConnValue());
            if (isSkip) {
                // Cho phép mọi chức năng ban đêm chọn bỏ qua.
                action.setTargetDeviceId(SKIP_TARGET);
                action.setTargetName("Bỏ qua");
                action.setTargetRoleId(0);
                action.setTargetRoleName("");
            } else {
                action.setTargetDeviceId(target.getIpData());
                action.setTargetName(target.getNameMember() != null ? target.getNameMember() : "Ẩn danh");
                action.setTargetRoleId(target.getId());
                action.setTargetRoleName(removeHtml(target.getRole()));
            }

            String key = actionKey(action);
            NightActionDto selected = nightSelections.get(key);
            if (selected != null && Objects.equals(selected.getTargetDeviceId(), action.getTargetDeviceId())) {
                nightSelections.remove(key);
                broadcastStateLocked();
                return buildState(actor.getIpData());
            }
            // Vấn đề 16: bảng cắn và bảng nguyền của Sói dùng chung 1 lượt chọn nên phải bỏ chọn bảng còn lại.
            String exclusiveWolfKey = exclusiveWolfActionKey(action);
            if (!ObjectUtils.isEmpty(exclusiveWolfKey)) {
                nightSelections.remove(exclusiveWolfKey);
            }
            nightSelections.put(key, action);
            if (allNightSelected() && nightRemainingSeconds > 5) {
                nightRemainingSeconds = 5;
            }
            broadcastStateLocked();
            return buildState(actor.getIpData());
        }
    }

    public Map<String, Object> submitDayNow() {
        synchronized (lock) {
            dayTimeoutSent = true;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event", "daySubmit");
            payload.put("state", buildState(null));
            messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
            return payload;
        }
    }

    public Map<String, Object> submitNightNow() {
        synchronized (lock) {
            nightTimeoutSent = true;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("event", "nightSubmit");
            payload.put("state", buildState(null));
            messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
            return payload;
        }
    }

    private void tick() {
        synchronized (lock) {
            boolean changed = false;
            // Đồng hồ ngày chỉ chạy khi admin bật bảng ngày
            if (showDayBoard && dayRemainingSeconds > 0) {
                dayRemainingSeconds--;
                changed = true;
                if (dayRemainingSeconds == 0 && !dayTimeoutSent) {
                    dayTimeoutSent = true;
                    submitDayLocked();
                    killDay();
                }
            }
            // Đồng hồ đêm chỉ chạy khi admin bật bảng đêm
            if (showNightBoard && nightRemainingSeconds > 0) {
                nightRemainingSeconds--;
                changed = true;
                if (nightRemainingSeconds == 0 && !nightTimeoutSent) {
                    nightTimeoutSent = true;
                    submitNightLocked();
                    killNight();
                    showNightBoard = false;
                }
            }
            if (showNightBoard && selectionCountdownSeconds > 0) {
                selectionCountdownSeconds--;
                changed = true;
                if (selectionCountdownSeconds > 5 && wolfAssassinSelected()) {
                    selectionCountdownSeconds = 5;
                }
            }
            if (showNightBoard && silentCountdownSeconds > 0) {
                silentCountdownSeconds--;
                changed = true;
                if (silentCountdownSeconds > 5 && silentSelected()) {
                    silentCountdownSeconds = 5;
                }
            }
            // Đồng hồ vote chỉ chạy khi admin bật bảng vote
            if (showVoteBoard && voteRemainingSeconds > 0) {
                voteRemainingSeconds--;
                changed = true;
                if (voteRemainingSeconds == 0 && !voteTimeoutSent) {
                    voteTimeoutSent = true;
                    submitVoteLocked();
                    killDay();// Chay lenh kill luon
                }
            }
            // Rút về 5s nếu tất cả đã chọn và còn nhiều hơn 5s
            if (showDayBoard && allDaySelected() && dayRemainingSeconds > 5) {
                dayRemainingSeconds = 5;
                changed = true;
            }
            if (showNightBoard && allNightSelected() && nightRemainingSeconds > 5) {
                nightRemainingSeconds = 5;
                changed = true;
            }
            if (showVoteBoard && allVoteSelected() && voteRemainingSeconds > 5) {
                voteRemainingSeconds = 5;
                changed = true;
            }
            if(showToughGuy && toughGuyRemainingSeconds > 0) {
                toughGuyRemainingSeconds--;
                changed = true;
                if (toughGuyRemainingSeconds == 0) {
                    toughGuyProcess();
                }
            }
            if (changed) {
                broadcastStateLocked();
            }
        }
    }

    private void submitDayLocked() {
        // Tự động tắt bảng ngày khi đồng hồ về 0 – broadcast để FE ẩn bảng
        showDayBoard = false;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "dayTimeout");
        payload.put("state", buildState(null));
        messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
    }

    private void submitNightLocked() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "nightTimeout");
        payload.put("state", buildState(null));
        messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
    }

    // Tự động tắt bảng vote khi đồng hồ về 0
    private void submitVoteLocked() {
        showVoteBoard = false;
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "voteTimeout");
        payload.put("state", buildState(null));
        messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
    }

    private void broadcastStateLocked() {
        messagingTemplate.convertAndSend(STATE_DESTINATION, buildState(null));
    }

    private void broadcastRefreshUiLocked(String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("event", "refreshUi");
        payload.put("reason", reason);
        payload.put("state", buildState(null));
        messagingTemplate.convertAndSend(STATE_DESTINATION, payload);
    }

    private Map<String, Object> buildState(String viewerDeviceId) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("dayRemainingSeconds", dayRemainingSeconds);
        state.put("nightRemainingSeconds", nightRemainingSeconds);
        state.put("voteRemainingSeconds", voteRemainingSeconds);
        // Vấn đề 2: thêm biến đếm ngược chung trong state.
        state.put("selectionCountdownSeconds", selectionCountdownSeconds);
        state.put("selectionCountdownRoleIds", ACTION_COUNTDOWN_ROLE_IDS);
        // Vấn đề 7: countdown riêng của Silent, xử lý role-based tương tự vấn đề 2.
        state.put("silentCountdownSeconds", silentCountdownSeconds);
        state.put("silentCountdownRoleIds", SILENT_COUNTDOWN_ROLE_IDS);
        state.put("daySelections", buildDaySelectionsView());
        state.put("voteSelections", buildVoteSelectionsView());
        // Vấn đề 1: action đêm được build từ service và gửi xuống state theo từng device.
        state.put("viewerActionsByDevice", buildViewerActionsByDevice());
        state.put("showDayBoard", showDayBoard);
        state.put("showNightBoard", showNightBoard);
        state.put("showVoteBoard", showVoteBoard);
        state.put("showToughGuy", showToughGuy);
        state.put("toughGuyRemainingSeconds", toughGuyRemainingSeconds);
        state.put("toughGuySelection", toughGuySelection);
        state.put("notShowDay", maSoiService.NOT_SHOW_DAY);
        // Khi bất kỳ bảng nào đang hiện, gửi danh sách người chơi mới nhất
        if (showDayBoard || showNightBoard || showVoteBoard || showToughGuy) {
            Map<String, String> players = new LinkedHashMap<>();
            for (DataMember p : alivePlayersRaw()) {
                if (!ObjectUtils.isEmpty(p.getIpData()) && !maSoiService.NOT_SHOW_DAY.contains(p.getIpData())) {
                    players.put(p.getIpData(), p.getNameMember() != null ? p.getNameMember() : "Ẩn danh");
                }
            }
            state.put("players", players);
            if (showVoteBoard) {
                DataMember votedPlayer = findPlayer(killDayDevice).orElse(new DataMember());
                state.put("votedPlayer", votedPlayer.getNameMember() + reasonKill);
            }
        }
        // Luôn gửi danh sách deviceId của người đã chết để FE ẩn bảng đúng mid-game
        List<String> deadIds = maSoiService.getPls().stream()
                .filter(p -> p.isDead() && !ObjectUtils.isEmpty(p.getIpData()))
                .map(DataMember::getIpData)
                .toList();
        state.put("deadDeviceIds", deadIds);
        state.put("nightRoleState", buildNightRoleState(viewerDeviceId));
        state.put("autoHistories", buildAutoHistoriesView());
        // Vấn đề 11: gửi list roleId không được tự chọn bản thân để FE disable tile tương ứng.
        state.put("selfSelectDisabledRoleIds", SELF_SELECT_DISABLED_ROLE_IDS);
        // Vấn đề 12: gửi map deviceId → oldTargetId để FE biết ai không được chọn lại.
        Map<String, String> playerOldTargets = new LinkedHashMap<>();
        for (DataMember p : maSoiService.getPls()) {
            if (!ObjectUtils.isEmpty(p.getIpData()) && !ObjectUtils.isEmpty(p.getOldTargetId())) {
                playerOldTargets.put(p.getIpData(), p.getOldTargetId());
            }
        }
        state.put("playerOldTargets", playerOldTargets);
        return state;
    }

    private List<String> buildAutoHistoriesView() {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : maSoiService.getAutoHistories()) {
            result.add(entry.getKey() + ": " + entry.getValue());
        }
        return result;
    }

    // Ví dụ tạm: trả về state riêng theo role của người đang xem bảng đêm.
    private Map<String, Object> buildNightRoleState(String viewerDeviceId) {
        Map<String, Object> nightRoleState = new LinkedHashMap<>();
        List<Map<String, Object>> wolfSelected = new ArrayList<>();
        List<Map<String, Object>> wolfRecruitSelected = new ArrayList<>();
        String seerResult = "";
        Set<DataMember> killedPlayer = new HashSet<>();
        for (Map.Entry<String, NightActionDto> entry : nightSelections.entrySet()) {
            NightActionDto value = entry.getValue();
            DataMember actor = findPlayer(value.getDeviceId()).orElse(new DataMember());
            DataMember target = findPlayer(value.getTargetDeviceId()).orElse(new DataMember());
            if (isWolfNightActor(value.getRoleId())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("actorDeviceId", actor.getIpData());
                if (ObjectUtils.isEmpty(target.getIpData())) {
                    item.put("targetDeviceId", SKIP_TARGET);
                    item.put("targetName", SKIP_TARGET);
                    item.put("isSkip", false);
                } else {
                    item.put("targetDeviceId", target.getIpData());
                    item.put("targetName", target.getNameMember() != null ? target.getNameMember() : "Ẩn danh");
                    item.put("isSkip", false);
                }
                item.put("actorName", actor.getNameMember() != null ? actor.getNameMember() : "Ẩn danh");
                item.put("colType", normalizeActionType(value.getColType()));
                if (RECRUIT.code.equals(item.get("colType"))) {
                    wolfRecruitSelected.add(item);
                } else {
                    wolfSelected.add(item);
                }
            }
            if (actor.getId() == 20 && selectionCountdownSeconds == 0 && !maSoiService.nextDayBlocks.containsKey(20)) {
                killedPlayer.add(target);
            }
            if (value.getRoleId() == 32) {
                MaSoiService.NightResolution resolution = new MaSoiService.NightResolution();
                for (Map.Entry<String, NightActionDto> entrySilent : nightSelections.entrySet()) {
                    if (entrySilent.getValue().getRoleId() == 38 && entrySilent.getValue().getTargetRoleId() == 32) {
                        resolution.disabledRoles.put(entrySilent.getValue().getTargetDeviceId(), " bị câm lặng");
                    }
                }
                List<NightActionDto> actions = List.of(value);
                Map<String, DataMember> playersByDevice = maSoiService.playersByDevice();
                seerResult = "Người được chọn là: " + maSoiService.applyConnectionForSeer(actions, playersByDevice, resolution);
            }
        }
        if (selectionCountdownSeconds == 0) {
            List<NightActionDto> actions = new ArrayList<>(nightSelections.values().stream()
                    .filter(action -> !SKIP_TARGET.equals(action.getTargetDeviceId()))
                    .toList());
            NightActionDto wolfAction = wolfAction(actions);
            if (!ObjectUtils.isEmpty(wolfAction.getRoleName()) && (wolfAction.actionType() == SOI || wolfAction.actionType() == RECRUIT)) {
                killedPlayer.add(findPlayer(wolfAction.getTargetDeviceId()).orElse(new DataMember()));
            }
        }

        DataMember viewer = findPlayer(viewerDeviceId).orElse(new DataMember());
        if (viewerDeviceId == null || viewer.getId() == 32) {
            nightRoleState.put("seerResult", seerResult); // TIEN TRI
        }
        if (viewerDeviceId == null || (maSoiService.WOLF_ROLE_IDS.contains(viewer.getId()) && viewer.getId() != 15)) {
            nightRoleState.put("wolfSelected", wolfSelected); // SOI
            // Vấn đề 16: bảng Nguyền dùng state riêng để FE hiển thị độc lập với bảng cắn.
            nightRoleState.put("wolfRecruitSelected", wolfRecruitSelected); // SOI NGUYEN
        }

        if (viewerDeviceId == null || viewer.getId() == 31) {
            nightRoleState.put("killedPlayer", killedPlayer); // PHU THuy
        }
        return nightRoleState;
    }

    // Tạo view danh sách vote để gửi xuống FE
    private List<Map<String, Object>> buildVoteSelectionsView() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : voteSelections.entrySet()) {
            DataMember actor = findPlayer(entry.getKey()).orElse(null);
            if (actor == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceId", actor.getIpData());
            item.put("name", actor.getNameMember() != null ? actor.getNameMember() : "Ẩn danh");
            item.put("answer", entry.getValue()); // "yes" hoặc "no"
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildDaySelectionsView() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, String> entry : daySelections.entrySet()) {
            DataMember actor = findPlayer(entry.getKey()).orElse(null);
            if (actor == null || maSoiService.NOT_SHOW_DAY.contains(actor.getIpData())) {
                continue;
            }
            // Skip entry: hiển thị riêng để FE biết người này đã bỏ qua
            if (SKIP_TARGET.equals(entry.getValue())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("actorDeviceId", actor.getIpData());
                item.put("actorName", actor.getNameMember() != null ? actor.getNameMember() : "Ẩn danh");
                item.put("targetDeviceId", SKIP_TARGET);
                item.put("targetName", "Bỏ qua");
                item.put("isSkip", true);
                result.add(item);
                continue;
            }
            DataMember target = findPlayer(entry.getValue()).orElse(null);
            if (target == null) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("actorDeviceId", actor.getIpData());
            item.put("actorName", actor.getNameMember() != null ? actor.getNameMember() : "Ẩn danh");
            item.put("targetDeviceId", target.getIpData());
            item.put("targetName", target.getNameMember() != null ? target.getNameMember() : "Ẩn danh");
            item.put("isSkip", false);
            result.add(item);
        }
        return result;
    }

    private List<DataMember> alivePlayersRaw() {
        return maSoiService.getPls().stream()
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()) && !maSoiService.NOT_SHOW_DAY.contains(player.getIpData()))
                .toList();
    }

    public Optional<DataMember> findPlayer(String deviceId) {
        if (ObjectUtils.isEmpty(deviceId)) {
            return Optional.empty();
        }
        return maSoiService.getPls().stream()
                .filter(player -> Objects.equals(player.getIpData(), deviceId))
                .findFirst();
    }

    private Optional<DataMember> findPlayer(int roleId) {
        return maSoiService.getPls().stream()
                .filter(player -> Objects.equals(player.getId(), roleId))
                .findFirst();
    }

    private boolean hasNightAction(DataMember player) {
        return player.getKillSkill() > 0
                || player.getProtectedSkill() > 0
                || player.isSuperProtectedSkill()
                || (player.getConnectSkill() > 0 && !HIDDEN_ID.contains(player.getId()));
    }

    private int nightActionSlots(List<DataMember> players) {
        int count = 0;
        for (DataMember player : players) {
            if (player.getKillSkill() > 0) {
                count++;
            }
            if (player.getProtectedSkill() > 0) {
                count++;
            }
            if (player.getConnectSkill() > 0 && !HIDDEN_ID.contains(player.getId())) {
                if (player.getId() != 42) {
                    count++;
                } else if (ObjectUtils.isEmpty(player.getOldTargetId())) {
                    count++;
                }
            }
            if (player.isSuperProtectedSkill()) {
                count++;
            }
        }
        return count;
    }

    private boolean allVoteSelected() {
        return alivePlayersRaw().size() > 0 && voteSelections.size() >= alivePlayersRaw().size();
    }

    private boolean allDaySelected() {
        return alivePlayersRaw().size() > 0 && daySelections.size() >= alivePlayersRaw().size();
    }

    private boolean silentSelected() {
        Optional<DataMember> silentPlayer = findPlayer(38);
        if (silentPlayer.isPresent()) {
            if (silentPlayer.get().isDead()) {
                if (500 < silentCountdownSecondRandom) {
                    silentCountdownSecondRandom = new Random().nextInt(6) + 7;;
                }
                silentCountdownSecondRandom--;
                if (silentCountdownSecondRandom <= 0) {
                    silentCountdownSeconds = 5;
                }
                return false;
            } else {
                // neu con song thi kiem tra xem action da chon hay chua
                return nightSelections.values().stream()
                        .anyMatch(value -> value.getRoleId() == 38);
            }
        } else {
            silentCountdownSeconds = 1;
            return false;
        }
    }

    private boolean wolfAssassinSelected() {
        Optional<DataMember> assassinPlayer = findPlayer(20);

        long aliveWolves = maSoiService.getPls().stream()
                .filter(player ->
                        MaSoiService.WOLF_ROLE_IDS.contains(player.getId())
                                && !player.isDead()
                                && player.getId() != 15)
                .count();
        long assassinActions = nightSelections.values().stream()
                .filter(value -> Objects.equals(value.getRoleId(), 20))
                .count();
        List<NightActionDto> wolfActions = nightSelections.values().stream()
                .filter(value ->
                        MaSoiService.WOLF_ROLE_IDS.contains(value.getRoleId())
                                && value.getRoleId() != 15)
                .toList();

        boolean assassinSelected =
                (assassinPlayer.isPresent() && assassinPlayer.get().isDead())
                        || assassinActions == 1;
        boolean wolfSelected =
                aliveWolves == 0
                        || (aliveWolves == wolfActions.size()
                        && wolfActions.stream()
                        .map(NightActionDto::getTargetDeviceId)
                        .collect(Collectors.toSet())
                        .size() == 1
                        && wolfActions.stream()
                        .map(action -> normalizeActionType(action.getColType()))
                        .collect(Collectors.toSet())
                        .size() == 1);

        boolean actionCheck = (assassinPlayer.isEmpty() || assassinSelected) && wolfSelected;

        if (assassinPlayer.map(DataMember::isDead).orElse(false) || aliveWolves == 0) {
            if (500 < selectionCountdownSecondRandom) {
                selectionCountdownSecondRandom = new Random().nextInt(6) + 9;
            }
            selectionCountdownSecondRandom--;
            return selectionCountdownSecondRandom <= 0 && actionCheck;
        }
        return actionCheck;
    }

    private boolean allNightSelected() {
        boolean isDead = Objects.equals(nightActionSlots(alivePlayersRaw()), nightActionSlots(maSoiService.getPls()));
        if (isDead) {
            if (nightRemainingSecondRandom > 500) {
                nightRemainingSecondRandom = new Random().nextInt(6) + 10;
            }
            nightRemainingSecondRandom--;
        } else {
            nightRemainingSecondRandom = 0;
        }
        if (selectionCountdownSeconds <= 0 && silentCountdownSeconds <= 0) {
            List<NightActionDto> actions = new ArrayList<>(nightSelections.values().stream()
                    .filter(action -> !ACTION_COUNTDOWN_ROLE_IDS.contains(action.getRoleId()) || action.getRoleId() == 38) // list ko chua soi, sat thu, silent vi da het gio roi
                    .toList());
            return nightRemainingSecondRandom <= 0 && actions.size() >= nightActionSlots(alivePlayersRaw().stream()
                    .filter(action -> !ACTION_COUNTDOWN_ROLE_IDS.contains(action.getId()) || action.getId() == 38) // list ko chua soi, sat thu, silent vi da het gio roi
                    .toList());
        } else {
            return nightActionSlots(alivePlayersRaw()) > 0 && nightSelections.size() >= nightActionSlots(alivePlayersRaw());
        }
    }

    private String actionKey(NightActionDto action) {
        return action.getDeviceId() + "|" + normalizeActionType(action.getColType());
    }

    private String exclusiveWolfActionKey(NightActionDto action) {
        if (action == null || !isWolfNightActor(action.getRoleId())) {
            return null;
        }
        String actionType = normalizeActionType(action.getColType());
        if (KILL.code.equals(actionType)) {
            return action.getDeviceId() + "|" + RECRUIT.code;
        }
        if (RECRUIT.code.equals(actionType)) {
            return action.getDeviceId() + "|" + KILL.code;
        }
        return null;
    }

    private boolean isWolfNightActor(int roleId) {
        return MaSoiService.WOLF_ROLE_IDS.contains(roleId) && roleId != 15;
    }

    private String normalizeActionType(String colType) {
        if (ObjectUtils.isEmpty(colType)) {
            return "conn";
        }
        return colType.trim().toLowerCase();
    }

    private String removeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("<[^>]*>", "").trim();
    }

    private Map<String, Object> errorState(String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", false);
        payload.put("message", message);
        return payload;
    }

    private Map<String, List<Map<String, Object>>> buildViewerActionsByDevice() {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (DataMember player : maSoiService.getPls()) {
            if (ObjectUtils.isEmpty(player.getIpData())) {
                continue;
            }
            result.put(player.getIpData(), buildViewerActions(player));
        }
        return result;
    }

    private List<Map<String, Object>> buildViewerActions(DataMember viewer) {
        if (viewer == null || viewer.isDead()) {
            return List.of();
        }
        List<Map<String, Object>> actions = new ArrayList<>();
        if (viewer.getKillSkill() > 0) {
            actions.add(buildAction("kill", "⚔️ Giết", "", false));
        }
        // Vấn đề 16: khi còn nguyền, Sói roleId <= 14 có thêm bảng Nguyền nhưng vẫn chỉ chọn 1 trong 2 bảng.
        if (isWolfNightActor(viewer.getId()) && maSoiService.isShowSoiNguyen()) {
            actions.add(buildAction(RECRUIT.code, "🪄 Nguyền", "", false));
        }
        if (viewer.getProtectedSkill() > 0) {
            actions.add(buildAction("prot", "🛡️ Bảo vệ", "", false));
        }
        int connectSkill = viewer.getConnectSkill();
        if (connectSkill > 0 && !HIDDEN_ID.contains(viewer.getId())) {
            actions.add(buildAction(
                    "conn",
                    connectSkill == 8 ? "🔮 Tiên tri" : "🔗 Kết nối",
                    String.valueOf(connectSkill),
                    viewer.getId() == 32
            ));
        }
        return actions;
    }

    private Map<String, Object> buildAction(String colType, String label, String connValue, boolean needsConfirm) {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("colType", colType);
        item.put("label", label);
        item.put("connValue", connValue);
        item.put("needsConfirm", needsConfirm);
        return item;
    }

    private void killDay() {
        try {
            Map<String, String> selections = ObjectUtils.isEmpty(voteSelections) ? daySelections : voteSelections;

            Map.Entry<String, Long> mostCommon = getMostCommon(selections);
            if (mostCommon == null) {
                return;
            }
            String target = mostCommon.getKey();
            long votes = mostCommon.getValue();

            if (isNeedVote) {
                if (ObjectUtils.isEmpty(voteSelections)) {
                    if (!Objects.equals(target, SKIP_TARGET) && votes * 2 > alivePlayersRaw().size()) {
                        killDayDevice = target;
                        reasonKill = reasonKills.get(new Random().nextInt(reasonKills.size()));
                        setShowVoteBoard(true);
                    }
                } else if (Objects.equals(target, "yes") && votes * 2 > alivePlayersRaw().size() && !ObjectUtils.isEmpty(killDayDevice)) {
                    daySelection(killDayDevice);
                }
            } else if (!Objects.equals(target, SKIP_TARGET) && votes * 2 > alivePlayersRaw().size()) {
                daySelection(target);
            }
        } finally {
            daySelections.clear();
            voteSelections.clear();
        }

    }

    private Map.Entry<String, Long> getMostCommon(Map<String, String> selections) {
        return selections.values().stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    private void killNight() {
        // toList() tạo immutable list, wolfAction có remove/add nên phải dùng ArrayList để tránh UnsupportedOperationException làm ticker dừng.
        List<NightActionDto> actions = new ArrayList<>(nightSelections.values().stream()
                .filter(action -> !SKIP_TARGET.equals(action.getTargetDeviceId()))
                .toList());

        wolfAction(actions);
        if (maSoiService.nextDayBlocks.containsKey(20)) {
            nextDayBlockHandle = true;
            actions.removeIf(action -> action.getRoleId() == 20);
        }
        if (nextDayBlockHandle) {
            nextDayBlockHandle = false;
            maSoiService.nextDayBlocks.clear();
        }
        maSoiService.processNight(actions);
        // neu skip thi clear het target
        nightSelections.values().stream().filter(value -> SKIP_TARGET.equals(value.getTargetDeviceId()))
            .forEach(value -> {
                findPlayer(value.getDeviceId()).ifPresent(player -> player.setOldTargetId(null));
            });
        nightSelections.clear();
        checkEndGame(false, null);
    }

    private NightActionDto wolfAction(List<NightActionDto> actions) {
        List<NightActionDto> wolfActions = actions.stream()
                .filter(action -> MaSoiService.WOLF_ROLE_IDS.contains(action.getRoleId()) && action.getRoleId() != 15)
                .toList();
        NightActionDto nightActionDto = new NightActionDto();
        actions.removeAll(wolfActions);
        if (maSoiService.nextDayBlocks.containsKey(1)) {
            nextDayBlockHandle = true;
            return nightActionDto;
        }
        if (wolfActions.size() == 1) {
            nightActionDto = wolfActions.get(0);
        } else if (wolfActions.size() > 1) {
            if (Objects.isNull(randomVictim)) {
                randomVictim = new Random().nextInt(wolfActions.size());
            }
            nightActionDto = wolfActions.stream()
                    .filter(action -> action.getRoleId() == 2 || action.getRoleId() == 4)
                    .findFirst()
                    .orElse(wolfActions.get(randomVictim));

        }
        if (!ObjectUtils.isEmpty(nightActionDto.getRoleName())) {
            String selectedWolfAction = normalizeActionType(nightActionDto.getColType());
            nightActionDto.setRoleName(SOI.code);
            nightActionDto.setColType(RECRUIT.code.equals(selectedWolfAction) ? RECRUIT.code : SOI.code);
            actions.add(nightActionDto);
        }
        return nightActionDto;
    }

    private void checkEndGame(boolean isDay, String target) {
        AtomicBoolean endGame = new AtomicBoolean(false);
        if (isDay) {
            findPlayer(target).ifPresent(dataMember -> {
               if (dataMember.getId() == 21) {
                    endGame("Chán đời chiến thắng");
                    endGame.set(true);
                }
            });
        }
        if (!endGame.get()) {
            endGameProcess();
        }
    }

    private void endGameProcess() {
        List<DataMember> alivePlayers = alivePlayersRaw();

        boolean hasWolf = alivePlayers.stream()
                .anyMatch(p -> p.getId() <= 14);

        boolean hasAssassin = alivePlayers.stream()
                .anyMatch(p -> p.getId() == 20);

        boolean allVillager = alivePlayers.stream()
                .noneMatch(p -> ACTION_COUNTDOWN_ROLE_IDS.contains(p.getId()));

        if (alivePlayers.isEmpty()) {
            endGame("Thế giới sụp đổ");
            return;
        }
        // Dân thắng
        if (allVillager) {
            endGame("Người dân chiến thắng");
            return;
        }

        // Chỉ còn Sói
        boolean isWolfWin = hasWolf && !hasAssassin;

        // Chỉ còn Sát thủ
        boolean isAssassinWin = !hasWolf && hasAssassin;

        if (isWolfWin) {
            checkWolfEndGame(alivePlayers);
        } else if (isAssassinWin) {
            checkAssassinEndGame(alivePlayers);
        }
    }

    private void checkWolfEndGame(List<DataMember> alivePlayers) {
        List<DataMember> wolves = alivePlayers.stream()
                .filter(p -> p.getId() <= 14)
                .toList();

        List<DataMember> villagers = alivePlayers.stream()
                .filter(p -> p.getId() >= 30)
                .toList();

        boolean isOneVillagerNoDamage = villagers.size() == 1
                && villagers.get(0).getKillSkill() == 0 && villagers.get(0).getId() != 35; // neu thanh nien cứng thì khác

        if (alivePlayers.stream().anyMatch(p -> p.getId() == 21)
                && !villagers.isEmpty()) {
            // neu con chan doi va dan game tiep tuc
            return;
        }

        if (wolves.size() == 1 && villagers.size() == 1) {
            int villagerId = villagers.get(0).getId();

            if (villagerId == 38) {
                endGame("Vòng lặp vô tận, sói chán quá bỏ đi");
            } else if (villagerId == 34) {
                endGame("Thế giới sụp đổ");
            }
        } else if (villagers.isEmpty() || isOneVillagerNoDamage) {
            endGame("Sói chiến thắng");
        }
    }

    private void checkAssassinEndGame(List<DataMember> alivePlayers) {
        List<DataMember> villagers = alivePlayers.stream()
                .filter(p -> p.getId() >= 30)
                .toList();

        if (alivePlayers.stream().anyMatch(p -> p.getId() == 21)
                && !villagers.isEmpty()) {
            // neu con chan doi va dan game tiep tuc
            return;
        }

        if (villagers.size() == 1) {
            int villagerId = villagers.get(0).getId();

            if (villagerId == 38) {
                endGame("Vòng lặp vô tận, sát thủ chán quá bỏ đi");
            } else if (villagerId == 34) {
                endGame("Thế giới sụp đổ");
            } else if (villagers.get(0).getKillSkill() == 0) {
                endGame("Sát thủ chiến thắng");
            }
        } else if (villagers.isEmpty()) {
            endGame("Sát thủ chiến thắng");
        }
    }

    private void endGame(String message) {
        maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>("End Game", " " + message));
        maSoiService.getCurrentGameHistory().put("End Game", " " + message);
        maSoiService.endGame();
    }

    private void toughGuyProcess() {
        if (!ObjectUtils.isEmpty(toughGuySelection)) {
            maSoiService.processDay(List.of(toughGuySelection));
            checkEndGame(false, null);
        } else {
            maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>("Không ai bị lôi ra chuồng gà", ""));
        }
        showToughGuy = false;
        toughGuySelection = "";
    }

    private void daySelection(String device) {
        Optional<DataMember> target = findPlayer(device);
        if (target.isPresent() && target.get().getId() == 35 && ObjectUtils.isEmpty(target.get().getOldTargetId())) {
            toughGuyRemainingSeconds = dayDurationSeconds;
            showToughGuy = true;
            maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>("Thanh niên cứng", target.get().getNameMember() + " có " + toughGuyRemainingSeconds + "s để lôi 1 người ra chuồng gà"));
            target.get().setOldTargetId("toughGay"); // ke ca ko vote thi thanh nien cung cung mat luot giet
        } else {
            maSoiService.processDay(List.of(device));
            checkEndGame(true, device);
            killDayDevice = null;
        }
    }
}
