package com.example.spyfall.service;

import com.example.spyfall.common.AutoSelectRequest;
import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.NightActionDto;
import lombok.Getter;
import lombok.Setter;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MaSoiAutoV1 {

    private static final String STATE_DESTINATION = "/ms/autoV1/state";
    private static final Set<Integer> HIDDEN_CONNECT_SKILLS = Set.of(5, 6);
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
    private int nightDurationSeconds = 150;
    @Getter
    private int voteDurationSeconds = 60;
    @Getter
    private int dayRemainingSeconds = 90;
    @Getter
    private int nightRemainingSeconds = 150;
    @Getter
    private int voteRemainingSeconds = 60;

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
    private boolean showNightBoard = true;
    @Getter
    private boolean showVoteBoard = false;
    private String killDayDevice = null;

    @Autowired
    public MaSoiAutoV1(MaSoiService maSoiService, SimpMessagingTemplate messagingTemplate) {
        this.maSoiService = maSoiService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostConstruct
    void startTicker() {
        scheduler.scheduleAtFixedRate(this::tick, 1, 1, TimeUnit.SECONDS);
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

    public Map<String, Object> updateDurations(int daySeconds, int nightSeconds, int voteSeconds, String viewerDeviceId) {
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
            }
            broadcastStateLocked();
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
            DataMember target = findPlayer(request.getTargetDeviceId()).orElse(null);
            if (actor == null || target == null || actor.isDead() || target.isDead()) {
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
            action.setConnValue(request.getConnValue());
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
            if (showNightBoard && allNightSelected() && nightRemainingSeconds > 5) { //TODO -----
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

    private Map<String, Object> buildState(String viewerDeviceId) {
        Map<String, Object> state = new LinkedHashMap<>();
        state.put("dayDurationSeconds", dayDurationSeconds);
        state.put("nightDurationSeconds", nightDurationSeconds);
        state.put("voteDurationSeconds", voteDurationSeconds);
        state.put("dayRemainingSeconds", dayRemainingSeconds);
        state.put("nightRemainingSeconds", nightRemainingSeconds);
        state.put("voteRemainingSeconds", voteRemainingSeconds);
        state.put("daySelections", buildDaySelectionsView());
        state.put("voteSelections", buildVoteSelectionsView());
        state.put("showDayBoard", showDayBoard);
        state.put("showNightBoard", showNightBoard);
        state.put("showVoteBoard", showVoteBoard);
        // Khi bất kỳ bảng nào đang hiện, gửi danh sách người chơi mới nhất
        if (showDayBoard || showNightBoard || showVoteBoard) {
            Map<String, String> players = new LinkedHashMap<>();
            for (DataMember p : alivePlayersRaw()) {
                if (!ObjectUtils.isEmpty(p.getIpData())) {
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
        state.put("autoHistories", maSoiService.getAutoHistories());
        return state;
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
            if (actor == null) {
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
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()))
                .toList();
    }

    private Optional<DataMember> findPlayer(String deviceId) {
        if (ObjectUtils.isEmpty(deviceId)) {
            return Optional.empty();
        }
        return maSoiService.getPls().stream()
                .filter(player -> Objects.equals(player.getIpData(), deviceId))
                .findFirst();
    }

    private boolean hasNightAction(DataMember player) {
        return player.getKillSkill() > 0
                || player.getProtectedSkill() > 0
                || (player.getConnectSkill() > 0 && !HIDDEN_CONNECT_SKILLS.contains(player.getConnectSkill()));
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
            if (player.getConnectSkill() > 0 && !HIDDEN_CONNECT_SKILLS.contains(player.getConnectSkill())) {
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

    private boolean allNightSelected() {
        return nightActionSlots(alivePlayersRaw()) > 0 && nightSelections.size() >= nightActionSlots(alivePlayersRaw());
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
                    maSoiService.processDay(List.of(killDayDevice));
                    killDayDevice = null;
                }
            } else if (!Objects.equals(target, SKIP_TARGET) && votes * 2 > alivePlayersRaw().size()) {
                maSoiService.processDay(List.of(target));
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
}
