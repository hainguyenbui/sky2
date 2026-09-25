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
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.spyfall.service.MaSoiAutoV1.*;
import static com.example.spyfall.service.MaSoiService.DAY_DEATH_REASONS;
import static com.example.spyfall.util.Constant.QR_MS_BLIND;

@Service
public class MaSoiBlindService {
    private static final Logger log = LoggerFactory.getLogger(MaSoiBlindService.class);

    private static final String STATE_DESTINATION = "/ms/blind/state";
    private static final String BLIND_SELECT_CODE = "blind-select";
    // Giá trị đặc biệt để đánh dấu người chơi chọn bỏ qua thay vì chọn ai
    private String reasonKill;

    private final MaSoiService maSoiService;
    private final MaSoiAutoService maSoiAutoService;
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
    @Getter
    private int voteRemainingSeconds = 45;

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

    private String killDayDevice = null;

    private static final String IMAGE_PATH = QR_MS_BLIND;

    public String getImage() {
        return IMAGE_PATH;
    }

    @Autowired
    public MaSoiBlindService(MaSoiService maSoiService, MaSoiAutoService maSoiAutoService, SimpMessagingTemplate messagingTemplate) {
        this.maSoiService = maSoiService;
        this.maSoiAutoService = maSoiAutoService;
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
            dayRemainingSeconds = dayDurationSeconds;
            nightRemainingSeconds = nightDurationSeconds;
            voteRemainingSeconds = voteDurationSeconds;
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
            if (show) {
                broadcastRefreshUiLocked("day-board-enabled");
            }
            return buildState(null);
        }
    }

    // Admin bật/tắt hiển thị bảng đêm; broadcast ngay để tất cả người chơi thấy
    public Map<String, Object> setShowNightBoard(boolean show) {
        synchronized (lock) {
            showNightBoard = show;
            if (show) {
                // Reset đồng hồ về mặc định khi bật lại để đếm ngược từ đầu
                nightRemainingSeconds = nightDurationSeconds;
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
            if (isSkip) {
                return errorState("Blind mode không có bỏ qua");
            }
            if (!ObjectUtils.isEmpty(actor.getOldTargetId()) && Objects.equals(actor.getOldTargetId(), targetDeviceId)) {
                return errorState("Không thể chọn lại người ở vòng trước");
            }

            NightActionDto action = new NightActionDto();
            action.setDeviceId(actor.getIpData());
            action.setRoleId(actor.getId());
            action.setRoleName(removeHtml(actor.getRole()));
            action.setTargetDeviceId(target.getIpData());
            action.setTargetName(target.getNameMember() != null ? target.getNameMember() : "Ẩn danh");
            action.setTargetRoleId(target.getId());
            action.setTargetRoleName(removeHtml(target.getRole()));

            String key = actionKey(action);
            NightActionDto selected = nightSelections.get(key);
            if (selected != null && Objects.equals(selected.getTargetDeviceId(), action.getTargetDeviceId())) {
                nightSelections.remove(key);
                broadcastStateLocked();
                return buildState(actor.getIpData());
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

    public DataMember getOrAssignRole(String deviceId, String playerName) {
        DataMember member = maSoiService.getOrAssignRole(deviceId, playerName);
        return DataMember.builder()
                .description("Bạn không biết mình là ai và phải tìm ra con Sói")
                .role("Ẩn vai")
                .idPlayGame(member.getIdPlayGame())
                .ipData(member.getIpData())
                .isDead(member.isDead())
                .oldTargetId(member.getOldTargetId())
                .nameMember(member.getNameMember())
                .build();
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
            // Đồng hồ đêm chỉ chạy chung cho toàn bộ role trong blind mode.
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
        // Blind mode chỉ có 1 countdown đêm cho tất cả role.
        state.put("dayRemainingSeconds", dayRemainingSeconds);
        state.put("nightRemainingSeconds", nightRemainingSeconds);
        state.put("voteRemainingSeconds", voteRemainingSeconds);
        state.put("daySelections", buildDaySelectionsView());
        state.put("voteSelections", buildVoteSelectionsView());
        state.put("viewerActionsByDevice", buildViewerActionsByDevice());
        state.put("showDayBoard", showDayBoard);
        state.put("showNightBoard", showNightBoard);
        state.put("showVoteBoard", showVoteBoard);
        state.put("deadDeviceIds", maSoiService.getDeadPls());

        state.put("notShowDay", maSoiService.NOT_SHOW_DAY);
        // Khi bất kỳ bảng nào đang hiện, gửi danh sách người chơi mới nhất
        if (showDayBoard || showNightBoard || showVoteBoard) {
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
        state.put("autoHistories", buildAutoHistoriesView());
        if (!ObjectUtils.isEmpty(viewerDeviceId)) {
            state.put("viewerSetting", maSoiAutoService.cloneSetting(maSoiAutoService.resolveViewerSetting(viewerDeviceId)));
        }
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

    private boolean allVoteSelected() {
        return alivePlayersRaw().size() > 0 && voteSelections.size() >= alivePlayersRaw().size();
    }

    private boolean allDaySelected() {
        return alivePlayersRaw().size() > 0 && daySelections.size() >= alivePlayersRaw().size();
    }

    private boolean allNightSelected() {
        List<DataMember> alivePlayers = alivePlayersRaw();
        return !alivePlayers.isEmpty() && nightSelections.size() >= alivePlayers.size();
    }

    private String actionKey(NightActionDto action) {
        return action.getDeviceId() + "|" + normalizeActionType(action.getColType());
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
        return List.of(buildAction(BLIND_SELECT_CODE, "Chọn 1 người chơi", "", false));
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
        List<NightActionDto> actions = new ArrayList<>(nightSelections.values().stream()
                .filter(action -> !SKIP_TARGET.equals(action.getTargetDeviceId()))
                .toList());
        processNight(actions);
        nightSelections.clear();
        checkEndGame();
    }

    private void daySelection(String device) {
        maSoiService.processDay(List.of(device));
        killDayDevice = null;
        checkEndGame();
    }

    private void processNight(List<NightActionDto> actions) {
        String silentTarget = actions.stream().filter(action -> action.getRoleId() == 38).findFirst().orElse(new NightActionDto()).getTargetDeviceId();
        NightActionDto woflKill = actions.stream().min(Comparator.comparing(NightActionDto::getRoleId)).orElse(new NightActionDto());
        NightActionDto protectMember = actions.stream().filter(action -> action.getRoleId() == 33 && !Objects.equals(action.getDeviceId(), silentTarget)).findFirst().orElse(null);
        StringBuilder nightDetails = new StringBuilder();

        if (Objects.equals(silentTarget, woflKill.getDeviceId())) {
            nightDetails.append("Sói đã cắn hụt ").append(woflKill.getTargetName()).append(": ").append(woflKill.getTargetRoleName()).append(" <br>");
            woflKill = null;
        } else {
            nightDetails.append("Sói đã cắn ").append(woflKill.getTargetName()).append(": ").append(woflKill.getTargetRoleName()).append(" <br>");
        }

        if (!ObjectUtils.isEmpty(protectMember)) {
            nightDetails.append(protectMember.getTargetName()).append(": ").append(protectMember.getTargetRoleName()).append(" đã được ").append(protectMember.getRoleName()).append(" bảo vệ <br>");
            if (!ObjectUtils.isEmpty(woflKill) && Objects.equals(woflKill.getTargetDeviceId(), protectMember.getTargetDeviceId())) {
                woflKill = null; // neu soi cắn nguoi duoc bao ve thi bo di
            }
        }

        for (NightActionDto action : actions) {
            // gan old target cho actor
            DataMember actor = findPlayer(action.getDeviceId()).orElse(new DataMember());
            actor.setOldTargetId(action.getTargetDeviceId());
            if (action.getRoleId() == 34) {
                nightDetails.append(actor.getNameMember()).append(" đã ghim ").append(action.getTargetName()).append(" <br>");
                if (!Objects.equals(silentTarget, action.getDeviceId()) && !ObjectUtils.isEmpty(woflKill) && Objects.equals(woflKill.getTargetDeviceId(), action.getDeviceId())) {
                    // nếu tho san bi loai thi keo theo nguoi di theo
                    nightDetails.append(action.getTargetName()).append(": ").append(action.getTargetRoleName()).append(" đã bị loại").append(" <br>");
                    findPlayer(action.getTargetDeviceId()).ifPresent(target -> {
                        maSoiService.getDeadPls().add(target);
                        maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>(target.getNameMember(), DAY_DEATH_REASONS.get(new Random().nextInt(DAY_DEATH_REASONS.size()))));
                    });
                }
            } else if (action.getRoleId() == 32) {
                nightDetails.append(actor.getNameMember()).append(" đã buộc ").append(action.getTargetName()).append(" nói thật <br>");
                if (!Objects.equals(silentTarget, action.getDeviceId())) {
                    maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>(action.getTargetName(), " phải nói thật <br>"));
                }
            } else if (action.getRoleId() == 46) {
                nightDetails.append(actor.getNameMember()).append(" đã câm lặng ").append(action.getTargetName()).append(" <br>");
                if (!Objects.equals(silentTarget, action.getDeviceId())) {
                    maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>(action.getTargetName(), " không được nói chuyện <br>"));
                }
            } else if (action.getRoleId() == 38) {
                nightDetails.append(actor.getNameMember()).append(" đã cấm sài phép ").append(action.getTargetName()).append(" <br>");
            } else if (action.getRoleId() == 45) {
                nightDetails.append(actor.getNameMember()).append(" đã đuổi ").append(action.getTargetName()).append(" ra khỏi làng <br>");
                if (!Objects.equals(silentTarget, action.getDeviceId())) {
                    maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>(action.getTargetName(), " đã bị đuổi ra khỏi làng TỐI TRỜ VỀ <br>"));
                    maSoiService.NOT_SHOW_DAY.add(action.getTargetDeviceId());
                }
            }
        }
        if (!ObjectUtils.isEmpty(woflKill)) {
            findPlayer(woflKill.getTargetDeviceId()).ifPresent(target -> {
                if (target.getId() == 40) {
                    // neu bi nguyen thi thanh soi
                    target.setId(4);
                } else {
                    maSoiService.getDeadPls().add(target);
                    maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>(target.getNameMember(), DAY_DEATH_REASONS.get(new Random().nextInt(DAY_DEATH_REASONS.size()))));
                }
            });
        }
        maSoiService.getCurrentGameHistory().put("Đêm " + maSoiService.nightNumber++, nightDetails.toString());
    }
    
    private void checkEndGame() {
        List<DataMember> alivePlayers = maSoiService.getPls().stream()
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()))
                .toList();
        if (alivePlayers.isEmpty()) {
            endGame("Thế giới sụp đổ");
        }
        if (alivePlayers.stream().noneMatch(p -> p.getId() > 10)) {
            endGame("Sói chiến thắng");
        } else if (alivePlayers.stream().noneMatch(p -> p.getId() <= 10)) {
            endGame("Dân chiến thắng");
        }
    }

    private void endGame(String message) {
        maSoiService.getAutoHistories().addFirst(new AbstractMap.SimpleEntry<>("End Game", " " + message));
        maSoiService.getCurrentGameHistory().put("End Game", " " + message);
        maSoiService.endGame();
    }
}
