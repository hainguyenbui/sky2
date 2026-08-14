package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.DayActionDto;
import com.example.spyfall.common.GameSessionState;
import com.example.spyfall.common.LifeLinkDto;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.common.NightGuideAction;
import com.example.spyfall.common.NightGuideStep;
import com.example.spyfall.common.RoundHistory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaSoiService {

    private enum GameLifecycle {
        NO_GAME,
        SETUP,
        IN_PROGRESS,
        RESETTING,
        FINISHED
    }

    private static final int ROLE_WOLF = 1;
    private static final int ROLE_ALPHA_WOLF = 2;
    private static final int ROLE_CURSED_WOLF = 3;
    private static final int ROLE_DOUBLE_CHECK_WOLF = 4;
    private static final int ROLE_TRAITOR = 15;
    private static final int ROLE_ASSASSIN = 20;
    private static final int ROLE_WITCH = 31;
    private static final int ROLE_SEER = 32;
    private static final int ROLE_GUARD = 33;
    private static final int ROLE_HUNTER = 34;
    private static final int ROLE_APPRENTICE_SEER = 37;
    private static final int ROLE_SILENCER = 38;
    private static final int ROLE_CURSED_VILLAGER = 40;
    private static final int ROLE_SICK_PERSON = 41;
    private static final int ROLE_CLONE = 42;
    private static final int ROLE_DICTATOR = 43;
    private static final int ROLE_ANGEL = 44;
    private static final int ROLE_OLD_WITCH = 45;
    private static final int ROLE_BOMB = 46;

    private List<DataMember> datas = new ArrayList<>();
    private List<DataMember> pls = new ArrayList<>();
    private List<DataMember> deadPls = new ArrayList<>();
    private List<DataMember> listShowForMember = new ArrayList<>();
    private boolean isGameEnd = true;
    private boolean curseAvailable = false;
    @Getter
    private boolean allowDeadViewGameHistory = false;
    @Getter
    private boolean allowShowAliveDead = false;
    private final Map<Integer, List<DataMember>> detailEachGame = new TreeMap<>(Comparator.reverseOrder());
    private final Map<Integer, List<RoundHistory>> playerArchivedHistory = new LinkedHashMap<>();
    private final Map<Integer, List<RoundHistory>> adminArchivedHistory = new LinkedHashMap<>();
    private final List<RoundHistory> playerCurrentHistory = new ArrayList<>();
    private final List<RoundHistory> adminCurrentHistory = new ArrayList<>();
    private final Map<String, Integer> seerInspectionCounts = new HashMap<>();
    private final Set<Long> discardedGameSessions = new LinkedHashSet<>();
    private final Set<Long> finishedGameSessions = new LinkedHashSet<>();
    private GameLifecycle gameLifecycle = GameLifecycle.NO_GAME;
    private long gameSessionSequence = 0;
    private long currentGameSessionId = 0;
    private boolean dayPhaseActive = false;
    private int oldWitchLastActionDay = 0;
    public boolean dayIsReadyKill = false;
    private final String image = "/qrcode.png";
    private final Map<String, List<String>> linkRole = new HashMap<>();// Liên kết sinh mệnh chức năng nếu key chết value sẽ có chức năng;
    private final List<String> howToDie = List.of(" bị thủ tiêu vì biết quá nhiều", " không muốn chơi nữa", " bị thù ghét", " nói quá nhiều");
    public int countNight = 1;

    public final List<Integer> ID_SOI = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    public final List<Integer> ID_OUTSIDER = List.of(21, 20, 22);

    public String getImage() { return image; }
    public List<DataMember> getDatas() throws Exception { dataInputService.prepareDataMaSoi(datas); return datas; }
    public List<DataMember> getPls() { return pls; }
    public List<DataMember> getDeadPls() { return deadPls; }
    public List<DataMember> getListShowForMember() { return listShowForMember; }
    public synchronized int getGameNumber() { return detailEachGame.size() + 1; }
    public synchronized List<RoundHistory> getPlayerCurrentHistory() { return List.copyOf(playerCurrentHistory); }
    public synchronized List<RoundHistory> getAdminCurrentHistory() { return List.copyOf(adminCurrentHistory); }
    public synchronized long getCurrentGameSessionId() { return currentGameSessionId; }
    public synchronized boolean canResetSetup() { return !isGameEnd && gameLifecycle == GameLifecycle.SETUP; }
    public synchronized boolean isGameStarted() { return !isGameEnd && gameLifecycle == GameLifecycle.IN_PROGRESS; }
    public synchronized void setAllowDeadViewGameHistory(boolean value) { allowDeadViewGameHistory = value; }
    public synchronized void setAllowShowAliveDead(boolean value) { allowShowAliveDead = value; }


    @Autowired
    DataInputService dataInputService;

    public synchronized String loadGame(Map<String, String> params) throws Exception{
        if (!isGameEnd) return "ALERT GAME NOT END YES";
        int totalPlay = Integer.parseInt(ObjectUtils.isEmpty(params.get("total")) ? "0" : params.get("total"));
        if (totalPlay == 0) return "ERROR LỖI KHÔNG TỔNG NGƯỜI";

        boolean isCupid = Boolean.parseBoolean(params.get("checkCupid"));

        dataInputService.prepareDataMaSoi(datas);
        resetCurrentGameState();
        currentGameSessionId = ++gameSessionSequence;
        gameLifecycle = GameLifecycle.SETUP;
        isGameEnd = false;

        int totalRole = 0;
        for (DataMember data : datas) {
            int total = 0;
            if (params.get(String.valueOf(data.getId())) != null) {
                total = Integer.parseInt(params.get(String.valueOf(data.getId())));
            }
            if (total >= 1) {
                listShowForMember.add(createDM(data, total));
            }
            for (int i = 0; i < total; i++) {
                data.setIpData("play");
                pls.add(DataMember.builder()
                        .id(data.getId()).idPlayGame(String.valueOf(pls.size() + 1))
                        .role(data.getRole()).description(data.getDescription())
                        .killSkill(data.getKillSkill()).protectedSkill(data.getProtectedSkill())
                        .connectSkill(data.getConnectSkill()).superProtectedSkill(data.isSuperProtectedSkill())
                        .inspectSkill(data.isInspectSkill())
                        .detailShow(data.getRole())
                        .build());
            }
            totalRole += total;
        }

        Random random = new Random();
        for (int i = 0; i < totalRole - totalPlay; i++) {
            int indexMember = random.nextInt(pls.size());
            pls.remove(indexMember);
        }

        curseAvailable = pls.stream().anyMatch(item -> item.getId() == ROLE_CURSED_WOLF);

        if (isCupid) {
            Collections.shuffle(pls);
            List<DataMember> cupid = pls.subList(0, 2);
            String c1 = cupid.get(0).getRole(), c2 = cupid.get(1).getRole();

            DataMember data1 = cupid.get(0), data2 = cupid.get(1);
            data1.setRole(c1 + " cặp đôi với " + c2);
            data1.setLifeLink(1);
            data1.setLifeLinkIds(List.of(data2.getId()));
            data2.setRole(c2 + " cặp đôi với " + c1);
            data2.setLifeLink(1);
            data2.setLifeLinkIds(List.of(data1.getId()));
        }
        return "OK load";
    }

    /**
     * Get or assign role for the player. Returns DataMember assigned or null if not set up.
     */
    public synchronized DataMember getOrAssignRole(String clientIp, String name) {
        if (pls.isEmpty()) return null;
        else {
            List<DataMember> sois = pls.stream().filter(player -> player.getId() < 15).toList();
            for (DataMember dataMember : pls) {
                if (clientIp.equals(dataMember.getIpData())) {
                    if (name != null) dataMember.setNameMember(name);
                    if (dataMember.getId() == 15) {
                        // neu la ke phan boi thi thay dc cac con soi
                        dataMember.setDetailShow("Sói là: " + sois.stream()
                                .map(DataMember::getNameMember)
                                .collect(Collectors.joining(", ")));
                    }
                    return dataMember;
                }
            }
        }

        Collections.shuffle(pls);
        pls.sort(Comparator.comparing(DataMember::getIpData, Comparator.nullsFirst(String::compareTo)));
        DataMember yourLocation = pls.get(0);
        if (yourLocation.getIpData() != null) {
            return new DataMember();
        }
        yourLocation.setIpData(clientIp);
        yourLocation.setNameMember(name);
        return yourLocation;
    }

    public synchronized Map<String, Object> getGameHistoryData() {
        List<Map<String, Object>> games = new ArrayList<>();
        detailEachGame.forEach((key, value) -> {
            Map<String, Object> game = new HashMap<>();
            game.put("gameNumber", key);
            
            List<Map<String, Object>> players = new ArrayList<>();
            value.stream()
                    .sorted(Comparator.comparing(DataMember::getId))
                    .forEach(item -> {
                        Map<String, Object> player = new HashMap<>();
                        String displayLocation = item.getRole().replaceAll("<[^>]*>", "");
                        String name = item.getNameMember() != null
                                ? item.getNameMember()
                                : (item.getIpData() != null ? "Ẩn danh" : "Chưa nhận");

                        player.put("id", item.getId());
                        player.put("displayName", name);
                        player.put("displayLocation", displayLocation);
                        player.put("isSoi", ID_SOI.contains(item.getId()));
                        player.put("isOutsider", ID_OUTSIDER.contains(item.getId()));
                        players.add(player);
                    });
            game.put("details", playerArchivedHistory.getOrDefault(key, List.of()));
            game.put("players", players);
            games.add(game);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("games", games);
        return result;
    }

    public synchronized String endGame() {
        if (isGameEnd) return "ERROR Không có ván đang hoạt động";
        if (gameLifecycle != GameLifecycle.IN_PROGRESS) {
            return "ERROR Ván chưa bắt đầu. Hãy dùng Thiết lập lại ván";
        }

        int gameNumber = detailEachGame.size() + 1;
        detailEachGame.put(gameNumber, snapshotPlayers(pls));
        playerArchivedHistory.put(gameNumber, List.copyOf(playerCurrentHistory));
        adminArchivedHistory.put(gameNumber, List.copyOf(adminCurrentHistory));
        rememberSession(finishedGameSessions, currentGameSessionId);
        isGameEnd = true;
        gameLifecycle = GameLifecycle.FINISHED;
        resetCurrentGameState();
        return "End Game : " + gameNumber;
    }

    public synchronized String discardSetup() {
        if (isGameEnd) return "ERROR Không có thiết lập đang hoạt động";
        if (gameLifecycle != GameLifecycle.SETUP) {
            return "ERROR Ván đã bắt đầu, không thể thiết lập lại mà không lưu lịch sử";
        }

        rememberSession(discardedGameSessions, currentGameSessionId);
        isGameEnd = true;
        gameLifecycle = GameLifecycle.RESETTING;
        resetCurrentGameState();
        return "OK Đã hủy thiết lập hiện tại";
    }

    public synchronized GameSessionState getGameSessionState(long requestedSessionId) {
        boolean requestedDiscarded = requestedSessionId > 0
                && discardedGameSessions.contains(requestedSessionId);
        boolean requestedFinished = requestedSessionId > 0
                && finishedGameSessions.contains(requestedSessionId);
        boolean replacementReady = requestedDiscarded && !isGameEnd
                && currentGameSessionId != requestedSessionId;
        return new GameSessionState(
                currentGameSessionId,
                gameLifecycle.name(),
                !isGameEnd,
                requestedDiscarded,
                requestedFinished,
                replacementReady);
    }

    public synchronized String getActiveRolesString() {
        Set<String> roleAll = new LinkedHashSet<>();
        List<Integer> notShow = List.of(21,30, 35, 41);
        for (DataMember item : pls.stream().sorted(Comparator.comparing(DataMember::getId)).toList()) {
            if (item.getId() < 15) {
                roleAll.add("Sói");
            } else if (!notShow.contains(item.getId())) {
                roleAll.add(item.getRole());
            }
        }
        return String.join(", ", roleAll);
    }

    public synchronized String processNight(List<NightActionDto> actions) {
        if (isGameEnd) return "ERROR Không có ván đang hoạt động";
        if (actions == null) return "ERROR Danh sách hành động không hợp lệ";

        Map<String, DataMember> playerByDevice = playersByDevice();
        String validationError = validateSpecialActions(actions, playerByDevice);
        if (validationError != null) return "ERROR " + validationError;

        dayIsReadyKill = false;
        Map<String, String> disabledRole = new LinkedHashMap<>();
        Map<String, String> disabledRoleNextDay = new LinkedHashMap<>();
        Map<String, String> toKill = new LinkedHashMap<>();
        Map<String, String> toSuperKill = new LinkedHashMap<>();
        Map<String, String> toSave = new LinkedHashMap<>();
        Map<String, LifeLinkDto> toLifeLink = new LinkedHashMap<>();
        List<String> adminEvents = new ArrayList<>();
        List<String> playerEvents = new ArrayList<>();

        boolean superProtectionActive = actions.stream().anyMatch(action -> {
            DataMember actor = playerByDevice.get(action.getDeviceId());
            return actor != null && actor.getId() == ROLE_ANGEL
                    && Objects.equals(action.getColType(), "superProt")
                    && Objects.equals(action.getConnValue(), "ON");
        });
        if (superProtectionActive) {
            adminEvents.add("Thiên thần đã bảo vệ toàn bộ người chơi trong đêm nay");
            pls.stream()
                    .filter(player -> player.getId() == ROLE_ANGEL && !player.isDead())
                    .findFirst()
                    .ifPresent(player -> player.setSuperProtectedSkill(false));
        }

        pls.forEach(player -> {
            if (player.getId() != ROLE_SICK_PERSON && player.getId() != 5) {
                player.setDisabledSkill(false);
            }
        });

        String alphaWolfDevice = checkAlphaWolf();
        Set<String> livingWolfDevices = pls.stream()
                .filter(this::isLivingWolf)
                .map(DataMember::getIpData)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Câm lặng được xử lý trước tất cả hành động còn lại trong đêm.
        for (NightActionDto action : actions) {
            DataMember actor = playerByDevice.get(action.getDeviceId());
            DataMember target = playerByDevice.get(action.getTargetDeviceId());
            if (actor == null || target == null || actor.getId() != ROLE_SILENCER || target.getId() == ROLE_ANGEL) {
                continue;
            }

            String event = displayName(target) + ": " + target.getRole() + " bị câm lặng";
            adminEvents.add(event);
            if (livingWolfDevices.contains(target.getIpData())) {
                if (Objects.equals(target.getIpData(), alphaWolfDevice)) disabledRole.put("soi", event);
                if (target.getId() == 5) disabledRole.put(target.getIpData(), event);
                if (target.getId() == ROLE_CURSED_WOLF) disabledRole.put("curse", event);
            } else {
                disabledRole.put(target.getIpData(), event);
            }
        }

        if (!superProtectionActive) {
            for (NightActionDto action : actions) {
                DataMember target = playerByDevice.get(action.getTargetDeviceId());
                if (target == null || target.getId() != ROLE_SICK_PERSON) continue;

                if (Objects.equals(action.getColType(), "soi")) {
                    pls.stream().filter(this::isLivingWolf).forEach(player -> player.setDisabledSkill(true));
                    disabledRoleNextDay.put("Sói", "Sói cắn trúng Người bệnh và mất lượt cắn đêm sau");
                } else if (Objects.equals(action.getColType(), "kill")) {
                    DataMember actor = playerByDevice.get(action.getDeviceId());
                    if (actor == null || actor.getId() != ROLE_ASSASSIN) continue;
                    actor.setDisabledSkill(true);
                    disabledRoleNextDay.put("Sát thủ", "Sát thủ cắn trúng Người bệnh và mất lượt đêm sau");
                }
            }
        }

        DataMember infectedWolf = pls.stream()
                .filter(player -> player.getId() == 5 && !player.isDead())
                .findFirst()
                .orElse(null);
        boolean infectedWolfTouched = false;

        for (NightActionDto action : actions) {
            DataMember actor = playerByDevice.get(action.getDeviceId());
            DataMember target = playerByDevice.get(action.getTargetDeviceId());

            if (infectedWolf != null && target == infectedWolf && actor != null && !infectedWolf.isDisabledSkill()) {
                if (!disabledRole.containsKey(infectedWolf.getIpData())) {
                    toKill.put(actor.getIpData(), "đụng phải Sói si đa");
                }
                infectedWolfTouched = true;
            }

            switch (Objects.requireNonNullElse(action.getColType(), "")) {
                case "soi" -> {
                    if (target == null || disabledRole.containsKey("soi")) break;
                    if (target.getId() == ROLE_CURSED_VILLAGER && !disabledRole.containsKey(target.getIpData())) {
                        convertToBasicWolf(target);
                        adminEvents.add(displayName(target) + " bị cắn và đã trở thành Sói");
                    } else if (target.getId() == ROLE_ASSASSIN) {
                        adminEvents.add("Sói cắn hụt Sát thủ " + displayName(target));
                    } else {
                        toKill.merge(target.getIpData(), "bị Sói cắn", this::joinReasons);
                    }
                }
                case "recruit" -> {
                    if (target == null) break;
                    if (disabledRole.containsKey("curse")) {
                        adminEvents.add("Sói nguyền bị câm lặng nên lời nguyền không được sử dụng");
                        break;
                    }
                    convertToBasicWolf(target);
                    curseAvailable = false;
                    adminEvents.add("Sói nguyền đã biến " + displayName(target) + " thành Sói");
                }
                case "inspect" -> {
                    if (actor == null || target == null) break;
                    if (disabledRole.containsKey(actor.getIpData())) {
                        adminEvents.add("Tiên tri " + displayName(actor) + " bị câm lặng nên không soi được");
                        break;
                    }
                    boolean detected = inspectTarget(target);
                    adminEvents.add("Tiên tri " + displayName(actor) + " soi " + displayName(target)
                            + ": " + (detected ? "Là Sói/Sát thủ" : "Không phải Sói/Sát thủ"));
                }
                case "kill" -> {
                    if (actor == null || target == null || disabledRole.containsKey(actor.getIpData())) break;
                    actor.decreaseKillSkill();
                    if (actor.getId() == ROLE_WITCH) {
                        toSuperKill.put(target.getIpData(), "bị " + actor.getRole() + " quăng bình");
                    } else {
                        if (actor.getId() == ROLE_DICTATOR && target.getId() >= 30) {
                            toKill.put(actor.getIpData(), "loại bỏ nhầm dân");
                        } else if (target.getId() < 30 && actor.getId() == ROLE_BOMB) {
                            adminEvents.add("Boom đã kích nổ mục tiêu không thuộc phe dân");
                            actor.setKillSkill(1);
                        }
                        toKill.merge(target.getIpData(), "bị " + actor.getRole() + " tác động vật lý", this::joinReasons);
                    }
                }
                case "prot" -> {
                    if (actor == null || target == null || disabledRole.containsKey(actor.getIpData())) break;
                    actor.decreaseProtectedSkill();
                    toSave.merge(target.getIpData(), protectionReason(actor, target), this::joinReasons);
                }
                case "conn" -> {
                    if (actor == null || target == null || disabledRole.containsKey(actor.getIpData())) break;
                    if (actor.getId() == ROLE_CLONE) {
                        linkRole.put(target.getIpData(), List.of(actor.getIpData()));
                        actor.setConnectSkill(0);
                        adminEvents.add("Nhân bản " + displayName(actor) + " đã chọn " + displayName(target)
                                + " (" + target.getRole() + ")");
                    } else if (actor.getId() == 34) {
                        toLifeLink.put(actor.getIpData(), new LifeLinkDto(target.getIpData(), "bị Thợ săn ghim"));
                    } else if (actor.getId() == 45) {
                        adminEvents.add("Phù thủy già đuổi " + displayName(target) + " ra khỏi làng");
                    }
                }
                default -> {
                    // superProt đã được xử lý trước để không phụ thuộc thứ tự action từ trình duyệt.
                }
            }
        }

        if (infectedWolfTouched) infectedWolf.setDisabledSkill(true);

        toSave.keySet().forEach(toKill::remove);
        toSuperKill.forEach((device, reason) -> toKill.merge(device, reason, this::joinReasons));

        int previousSize;
        do {
            previousSize = toKill.size();
            for (Map.Entry<String, LifeLinkDto> entry : toLifeLink.entrySet()) {
                if (toKill.containsKey(entry.getKey())) {
                    toKill.merge(entry.getValue().getDeviceId(), entry.getValue().getResonKill(), this::joinReasons);
                }
            }
            for (String device : new ArrayList<>(toKill.keySet())) {
                DataMember player = playerByDevice.get(device);
                if (player == null || player.getLifeLink() <= 0 || player.getLifeLinkIds() == null) continue;
                for (Integer linkedId : player.getLifeLinkIds()) {
                    pls.stream()
                            .filter(candidate -> !candidate.isDead() && candidate.getId() == linkedId)
                            .findFirst()
                            .ifPresent(linked -> toKill.putIfAbsent(linked.getIpData(), "chết theo liên kết tình yêu"));
                }
            }
        } while (previousSize != toKill.size());

        if (superProtectionActive) toKill.clear();
        disabledRole.values().forEach(event -> addUnique(adminEvents, event));
        disabledRoleNextDay.values().forEach(event -> addUnique(adminEvents, event));

        String result = resolveNight(toKill, playerByDevice, toSave, adminEvents, playerEvents);
        gameLifecycle = GameLifecycle.IN_PROGRESS;
        return result;
    }

    public synchronized String processDay(List<String> deviceIds) {
        if (deviceIds == null) return "ERROR Danh sách người chơi không hợp lệ";

        dayIsReadyKill = true;
        Map<String, DataMember> playerByDevice = playersByDevice();
        Map<String, DataMember> eliminated = new LinkedHashMap<>();
        List<String> playerEvents = new ArrayList<>();
        List<String> adminEvents = new ArrayList<>();
        Random random = new Random();

        for (String deviceId : new LinkedHashSet<>(deviceIds)) {
            DataMember player = playerByDevice.get(deviceId);
            if (player == null || player.isDead()) continue;

            eliminateDuringDay(player, howToDie.get(random.nextInt(howToDie.size())),
                    eliminated, playerEvents, adminEvents);
        }

        eliminated.forEach((device, player) -> applyDeathEffects(device, player, playerByDevice, adminEvents));
        if (playerEvents.isEmpty()) {
            playerEvents.add("Không có ai bị loại qua biểu quyết");
            adminEvents.add("Không có ai bị loại qua biểu quyết");
        }

        String label = "Ngày " + currentDayNumber();
        appendRoundHistory(playerCurrentHistory, label, playerEvents);
        appendRoundHistory(adminCurrentHistory, label, adminEvents);
        dayPhaseActive = false;
        return "Detail: " + String.join(System.lineSeparator(), adminEvents);
    }

    public synchronized String processDayRole(DayActionDto action) {
        if (action == null || !Objects.equals(action.type(), "oldWitch")) {
            return "ERROR Hành động ban ngày không hợp lệ";
        }
        if (!dayPhaseActive) return "ERROR Chưa đến pha ban ngày";

        Map<String, DataMember> playerByDevice = playersByDevice();
        DataMember actor = playerByDevice.get(action.actorDeviceId());
        DataMember target = playerByDevice.get(action.targetDeviceId());
        if (actor == null || actor.isDead() || actor.getId() != ROLE_OLD_WITCH) {
            return "ERROR Phù thủy già không còn hợp lệ";
        }
        if (target == null || target.isDead()) return "ERROR Mục tiêu không còn hợp lệ";
        if (actor == target) return "ERROR Phù thủy già không thể tự đuổi mình";

        int dayNumber = currentDayNumber();
        if (oldWitchLastActionDay == dayNumber) return "ERROR Phù thủy già đã hành động trong ngày này";

        Map<String, DataMember> eliminated = new LinkedHashMap<>();
        List<String> playerEvents = new ArrayList<>();
        List<String> adminEvents = new ArrayList<>();
        eliminateDuringDay(target, " bị Phù thủy già đuổi khỏi làng",
                eliminated, playerEvents, adminEvents);
        eliminated.forEach((device, player) -> applyDeathEffects(device, player, playerByDevice, adminEvents));

        String label = "Ngày " + dayNumber;
        appendRoundHistory(playerCurrentHistory, label, playerEvents);
        appendRoundHistory(adminCurrentHistory, label, adminEvents);
        oldWitchLastActionDay = dayNumber;
        return "Detail: " + String.join(System.lineSeparator(), adminEvents);
    }

    private void eliminateDuringDay(DataMember player, String reason,
                                    Map<String, DataMember> eliminated,
                                    List<String> playerEvents, List<String> adminEvents) {
        if (player == null || player.isDead()) return;

        markDead(player);
        eliminated.put(player.getIpData(), player);
        String event = displayName(player) + ": " + player.getRole() + reason;
        playerEvents.add(event);
        adminEvents.add(event);

        if (player.getLifeLink() <= 0 || player.getLifeLinkIds() == null) return;
        for (Integer linkedId : player.getLifeLinkIds()) {
            pls.stream()
                    .filter(candidate -> !candidate.isDead() && candidate.getId() == linkedId)
                    .findFirst()
                    .ifPresent(linked -> eliminateDuringDay(linked, " chết theo liên kết tình yêu",
                            eliminated, playerEvents, adminEvents));
        }
    }

    private void appendRoundHistory(List<RoundHistory> history, String label, List<String> events) {
        if (!history.isEmpty() && Objects.equals(history.get(history.size() - 1).label(), label)) {
            List<String> combined = new ArrayList<>(history.get(history.size() - 1).events());
            combined.addAll(events);
            history.set(history.size() - 1, new RoundHistory(label, combined));
        } else {
            history.add(new RoundHistory(label, events));
        }
    }

    /**
     * Lấy ra sói đầu đàn hoặc là con sói duy nhất
     * @return id sói
     */
    private String checkAlphaWolf() {
        List<DataMember> livingWolves = pls.stream().filter(this::isLivingWolf).toList();
        if (livingWolves.size() == 1) return livingWolves.get(0).getIpData();

        return livingWolves.stream()
                .filter(player -> player.getId() == ROLE_ALPHA_WOLF || player.getId() == ROLE_DOUBLE_CHECK_WOLF)
                .map(DataMember::getIpData)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseGet(() -> livingWolves.stream()
                        .map(DataMember::getIpData)
                        .filter(Objects::nonNull)
                        .findFirst()
                        .orElse(null));
    }

    public synchronized Map<String, Object> getGameManagementData() {
        Map<String, Object> data = new HashMap<>();
        LinkedHashMap<String, Map<String, Object>> colMap = new LinkedHashMap<>();
        boolean hasSuperProtectedPlayers = false;
        String actingWolfDevice = checkAlphaWolf();
        DataMember actingWolf = playersByDevice().get(actingWolfDevice);
        if (actingWolf != null) {
            Map<String, Object> wolfColumn = createActionColumn(
                    ROLE_WOLF, "🐺 Sói", actingWolf, true,
                    false, false, false, false, false, 0);
            colMap.put("soi", wolfColumn);
        }

        for (DataMember player : pls) {
            if (player.isDead() || ObjectUtils.isEmpty(player.getIpData())) continue;
            int roleId = player.getId();
            if (player.isSuperProtectedSkill()) {
                hasSuperProtectedPlayers = true;
            }
            if (roleId == ROLE_OLD_WITCH) continue;
            boolean isSoi   = ID_SOI.contains(roleId) && roleId != 15;
            boolean hasKill = player.getKillSkill() > 0;
            boolean hasProt = player.getProtectedSkill() > 0;
            boolean hasConn = player.getConnectSkill() > 0 && !List.of(6,5,4).contains( player.getConnectSkill());
            boolean hasInspect = player.isInspectSkill();

            if (roleId == ROLE_CURSED_WOLF && curseAvailable && !colMap.containsKey("curse")) {
                Map<String, Object> curseColumn = createActionColumn(
                        ROLE_CURSED_WOLF, "🌘 Sói nguyền", player, false,
                        false, false, false, false, true, 0);
                colMap.put("curse", curseColumn);
            }

            if (!isSoi && (hasKill || hasProt || hasConn || hasInspect)
                    && !colMap.containsKey(String.valueOf(roleId))) {
                Map<String, Object> col = createActionColumn(
                        roleId, player.getRole().replaceAll("<[^>]*>", ""), player, false,
                        hasKill, hasProt, hasConn, hasInspect, false, player.getConnectSkill());
                colMap.put(String.valueOf(roleId), col);
            }
        }
        var sorted = colMap.values().stream().sorted(Comparator.comparing(m -> (Integer) m.get("roleId"))).toList();

        // Build player rows. Each player carries their current skill state.
        List<Map<String, Object>> players = new ArrayList<>();
        for (DataMember player : pls) {
            if(player.isDead() || ObjectUtils.isEmpty(player.getIpData())) continue;
            Map<String, Object> p = new HashMap<>();
            int id   = player.getId();
            String name = player.getNameMember() != null ? player.getNameMember() : "Ẩn danh";
            p.put("deviceId",          player.getIpData());
            p.put("idPlayGame",        player.getIdPlayGame());
            p.put("name",              name);
            p.put("roleName",          player.getRole().replaceAll("<[^>]*>", ""));
            p.put("id",                id);
            p.put("wolfFaction",       ID_SOI.contains(id));
            // Sói thật, không tính Kẻ phản bội: đây là những người phe Sói không được cắn.
            p.put("realWolf",          isLivingWolf(player));
            p.put("hasSuperProtected", player.isSuperProtectedSkill());
            p.put("inspectResult",      inspectResultText(player));
            p.put("contactWarning",     id == 5 ? "Chạm Sói si đa sẽ khiến role này chết" : "");
            players.add(p);
        }
        players.sort(Comparator.comparing(m -> (Integer) m.get("id")));

        DataMember angel = pls.stream()
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()))
                .filter(player -> player.getId() == ROLE_ANGEL && player.isSuperProtectedSkill())
                .findFirst()
                .orElse(null);
        List<NightGuideStep> nightGuideSteps = buildNightGuideSteps(sorted, angel);
        DataMember oldWitch = pls.stream()
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()))
                .filter(player -> player.getId() == ROLE_OLD_WITCH)
                .findFirst()
                .orElse(null);

        data.put("players",                  players);
        data.put("columns",                  new ArrayList<>(sorted));
        data.put("hasSuperProtectedPlayers", hasSuperProtectedPlayers);
        data.put("nightGuideSteps",           nightGuideSteps);
        data.put("dayPhaseActive",            dayPhaseActive);
        data.put("oldWitchAction",            buildOldWitchAction(oldWitch));
        data.put("currentAdminHistory",       List.copyOf(adminCurrentHistory));
        data.put("archivedAdminGames",        buildAdminArchivedGames());
        data.put("gameNumber",                getGameNumber());
        data.put("gameActive",                !isGameEnd);
        data.put("canResetSetup",             canResetSetup());
        data.put("gameStarted",               isGameStarted());
        data.put("gameSessionId",             currentGameSessionId);

        return data;
    }

    private Map<String, Object> createActionColumn(int roleId, String label, DataMember actor,
                                                    boolean wolfBite, boolean kill, boolean protect,
                                                    boolean connect, boolean inspect, boolean recruit,
                                                    int connectValue) {
        Map<String, Object> column = new HashMap<>();
        column.put("roleId", roleId);
        column.put("label", label);
        column.put("isSoi", wolfBite);
        column.put("hasKill", kill);
        column.put("hasProt", protect);
        column.put("hasConn", connect);
        column.put("hasInspect", inspect);
        column.put("hasRecruit", recruit);
        column.put("connValue", connectValue);
        column.put("disabled", actor.isDisabledSkill());
        column.put("deviceId", actor.getIpData());
        int subColumns = (wolfBite ? 1 : 0) + (kill ? 1 : 0) + (protect ? 1 : 0)
                + (connect ? 1 : 0) + (inspect ? 1 : 0) + (recruit ? 1 : 0);
        column.put("subCols", subColumns);
        return column;
    }

    private List<NightGuideStep> buildNightGuideSteps(Collection<Map<String, Object>> columns, DataMember angel) {
        List<NightGuideAction> actions = new ArrayList<>();
        if (angel != null) {
            actions.add(new NightGuideAction(
                    "superProt_" + ROLE_ANGEL,
                    "superProt",
                    "🛡️",
                    "Bật bảo vệ toàn bộ đêm",
                    ROLE_ANGEL,
                    "Thiên thần",
                    angel.getIpData(),
                    "ON",
                    false,
                    "toggle",
                    true,
                    "bật bảo vệ toàn đêm"));
        }

        for (Map<String, Object> column : columns) {
            if (Boolean.TRUE.equals(column.get("isSoi"))) addGuideAction(actions, column, "soi");
            if (Boolean.TRUE.equals(column.get("hasKill"))) addGuideAction(actions, column, "kill");
            if (Boolean.TRUE.equals(column.get("hasProt"))) addGuideAction(actions, column, "prot");
            if (Boolean.TRUE.equals(column.get("hasConn"))) addGuideAction(actions, column, "conn");
            if (Boolean.TRUE.equals(column.get("hasInspect"))) addGuideAction(actions, column, "inspect");
            if (Boolean.TRUE.equals(column.get("hasRecruit"))) addGuideAction(actions, column, "recruit");
        }

        Map<String, List<NightGuideAction>> grouped = new LinkedHashMap<>();
        for (NightGuideAction action : actions) {
            grouped.computeIfAbsent(guideStepKey(action), ignored -> new ArrayList<>()).add(action);
        }

        return grouped.entrySet().stream()
                .map(entry -> createGuideStep(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(NightGuideStep::priority))
                .toList();
    }

    private void addGuideAction(List<NightGuideAction> actions, Map<String, Object> column, String type) {
        int roleId = (Integer) column.get("roleId");
        String roleName = String.valueOf(column.get("label"));
        String icon = switch (type) {
            case "soi" -> "🐺";
            case "recruit" -> "🌘";
            case "kill" -> "⚔️";
            case "prot" -> "🛡️";
            case "inspect" -> "🔮";
            default -> "🔗";
        };
        String label;
        if (roleId == ROLE_WITCH && Objects.equals(type, "kill")) label = "Dùng bình Độc";
        else if (roleId == ROLE_WITCH && Objects.equals(type, "prot")) label = "Dùng bình Cứu";
        else if (roleId == ROLE_SILENCER && Objects.equals(type, "conn")) label = "Chọn người bị câm";
        else if (roleId == ROLE_CLONE && Objects.equals(type, "conn")) label = "Chọn người để nhân bản";
        else if (roleId == ROLE_HUNTER && Objects.equals(type, "conn")) label = "Chọn người bị ghim";
        else {
            label = switch (type) {
                case "soi" -> "Chọn người bị cắn";
                case "recruit" -> "Dùng lời nguyền thay lượt cắn";
                case "prot" -> "Chọn người được bảo vệ";
                case "inspect" -> "Chọn người cần soi";
                default -> "Chọn mục tiêu";
            };
        }

        // Động từ ở bước tổng kết. Bám theo đúng cách phân nhánh của nhãn phía trên
        // để quản trò đọc thành câu: "Phù thủy dùng bình Cứu cho Hà 5".
        String verb;
        if (roleId == ROLE_WITCH && Objects.equals(type, "kill")) verb = "dùng bình Độc lên";
        else if (roleId == ROLE_WITCH && Objects.equals(type, "prot")) verb = "dùng bình Cứu cho";
        else if (roleId == ROLE_SILENCER && Objects.equals(type, "conn")) verb = "làm câm";
        else if (roleId == ROLE_CLONE && Objects.equals(type, "conn")) verb = "sao chép";
        else if (roleId == ROLE_HUNTER && Objects.equals(type, "conn")) verb = "ghim";
        else {
            verb = switch (type) {
                case "soi" -> "cắn";
                case "recruit" -> "nguyền";
                // "che chở" chứ không phải "bảo vệ", nếu không sẽ đọc thành "Bảo vệ bảo vệ An 1"
                case "prot" -> "che chở";
                case "inspect" -> "soi";
                case "kill" -> "giết";
                default -> "chọn";
            };
        }

        String targetPolicy;
        if (roleId == ROLE_WITCH && Objects.equals(type, "prot")) targetPolicy = "witchSave";
        else if (roleId == ROLE_SILENCER && Objects.equals(type, "conn")) targetPolicy = "silence";
        else if (roleId == ROLE_CLONE && Objects.equals(type, "conn")) targetPolicy = "noSelf";
        else if (Objects.equals(type, "recruit")) targetPolicy = "recruit";
        else if (Objects.equals(type, "inspect")) targetPolicy = "noSelf";
        else if (Objects.equals(type, "soi")) targetPolicy = "wolfBite";
        else targetPolicy = "any";

        actions.add(new NightGuideAction(
                type + "_" + roleId,
                type,
                icon,
                label,
                roleId,
                roleName,
                String.valueOf(column.get("deviceId")),
                String.valueOf(column.getOrDefault("connValue", "")),
                Boolean.TRUE.equals(column.get("disabled")),
                targetPolicy,
                false,
                verb));
    }

    private String guideStepKey(NightGuideAction action) {
        if (action.roleId() == ROLE_SILENCER) return "silence";
        if (action.roleId() == ROLE_ANGEL) return "angel";
        if (action.roleId() == ROLE_CLONE) return "clone";
        if (action.roleId() == ROLE_HUNTER) return "hunter";
        if (action.roleId() == ROLE_GUARD) return "guard";
        if (Objects.equals(action.type(), "soi") || Objects.equals(action.type(), "recruit")) return "wolves";
        if (action.roleId() == ROLE_WITCH) return "witch";
        if (action.roleId() == ROLE_ASSASSIN) return "assassin";
        if (action.roleId() == ROLE_DICTATOR) return "dictator";
        if (action.roleId() == ROLE_BOMB) return "bomb";
        if (action.roleId() == ROLE_SEER) return "seer";
        return "role-" + action.roleId();
    }

    private NightGuideStep createGuideStep(String key, List<NightGuideAction> actions) {
        NightGuideAction first = actions.get(0);
        return switch (key) {
            case "silence" -> new NightGuideStep(key, "🤫", "Câm lặng",
                    "Gọi Câm lặng thức dậy và chọn người bị khóa chức năng đêm nay.",
                    "Câm lặng nhắm mắt.", 10, actions);
            case "angel" -> new NightGuideStep(key, "🛡️", "Thiên thần",
                    "Gọi Thiên thần. Chỉ bật khi muốn ngăn toàn bộ cái chết trong đêm.",
                    "Thiên thần nhắm mắt.", 20, actions);
            case "clone" -> new NightGuideStep(key, "🔗", "Nhân bản",
                    "Gọi Nhân bản. Bước này xuất hiện mỗi đêm cho đến khi đã chọn một người.",
                    "Nhân bản nhắm mắt.", 30, actions);
            case "hunter" -> new NightGuideStep(key, "🏹", "Thợ săn",
                    "Gọi Thợ săn và chọn người sẽ chết theo nếu Thợ săn chết trong đêm.",
                    "Thợ săn nhắm mắt.", 40, actions);
            case "guard" -> new NightGuideStep(key, "🛡️", "Bảo vệ",
                    "Gọi Bảo vệ và chọn người được bảo vệ trước khi Sói hành động.",
                    "Bảo vệ nhắm mắt.", 50, actions);
            case "wolves" -> new NightGuideStep(key, "🐺", "Phe Sói",
                    "Gọi phe Sói. Chọn Cắn hoặc Nguyền, không thể chọn cả hai.",
                    "Phe Sói nhắm mắt.", 60, actions);
            case "witch" -> new NightGuideStep(key, "🧪", "Phù thủy",
                    "Cho Phù thủy biết mục tiêu Sói cắn, sau đó quyết định Cứu và dùng Độc.",
                    "Phù thủy nhắm mắt.", 70, actions);
            case "assassin" -> new NightGuideStep(key, "🗡️", "Sát thủ",
                    "Gọi Sát thủ và chọn mục tiêu.",
                    "Sát thủ nhắm mắt.", 80, actions);
            case "dictator" -> new NightGuideStep(key, "⚔️", "Độc tài",
                    "Gọi Độc tài nếu muốn sử dụng quyền giết duy nhất.",
                    "Độc tài nhắm mắt.", 90, actions);
            case "bomb" -> new NightGuideStep(key, "💣", "Boooooom",
                    "Gọi Boom và chọn mục tiêu kích nổ.",
                    "Boom nhắm mắt.", 100, actions);
            case "seer" -> new NightGuideStep(key, "🔮", "Tiên tri",
                    "Gọi Tiên tri, chọn mục tiêu và ra hiệu theo kết quả chỉ admin nhìn thấy.",
                    "Tiên tri nhắm mắt.", 110, actions);
            default -> new NightGuideStep(key, first.icon(), first.roleName(),
                    "Gọi " + first.roleName() + " và chọn hành động.",
                    first.roleName() + " nhắm mắt.", 105 + first.roleId(), actions);
        };
    }

    private Map<String, Object> buildOldWitchAction(DataMember oldWitch) {
        if (!dayPhaseActive || oldWitch == null || oldWitchLastActionDay == currentDayNumber()) return null;
        Map<String, Object> action = new LinkedHashMap<>();
        action.put("actorDeviceId", oldWitch.getIpData());
        action.put("actorName", displayName(oldWitch));
        action.put("roleName", oldWitch.getRole().replaceAll("<[^>]*>", ""));
        return action;
    }

    private int currentDayNumber() {
        return Math.max(1, countNight - 1);
    }

    private String inspectResultText(DataMember target) {
        boolean detected = target.getId() == ROLE_DOUBLE_CHECK_WOLF
                ? seerInspectionCounts.getOrDefault(target.getIpData(), 0) >= 1
                : (target.getId() >= ROLE_WOLF && target.getId() < ROLE_TRAITOR)
                || target.getId() == ROLE_ASSASSIN;
        return detected ? "Là Sói/Sát thủ" : "Không phải Sói/Sát thủ";
    }

    private String validateSpecialActions(List<NightActionDto> actions, Map<String, DataMember> playerByDevice) {
        boolean hasWolfBite = false;
        boolean hasCurse = false;
        Set<String> selections = new HashSet<>();
        String wolfBiteTarget = actions.stream()
                .filter(action -> Objects.equals(action.getColType(), "soi"))
                .map(NightActionDto::getTargetDeviceId)
                .findFirst()
                .orElse(null);

        for (NightActionDto action : actions) {
            String type = Objects.requireNonNullElse(action.getColType(), "");
            DataMember actor = playerByDevice.get(action.getDeviceId());
            if (Objects.equals(type, "superProt")) {
                if (actor == null || actor.isDead() || actor.getId() != ROLE_ANGEL
                        || !actor.isSuperProtectedSkill()) {
                    return "Thiên thần không còn quyền bảo vệ toàn bộ";
                }
                if (!selections.add(type + ":" + actor.getIpData())) {
                    return "Mỗi chức năng chỉ được chọn một lần";
                }
                continue;
            }

            DataMember target = playerByDevice.get(action.getTargetDeviceId());
            if (actor == null || actor.isDead()) return "Người thực hiện hành động không còn hợp lệ";
            if (target == null || target.isDead()) return "Mục tiêu không còn hợp lệ";

            String selectionKey = type + ":" + actor.getIpData();
            if (!selections.add(selectionKey)) return "Mỗi chức năng chỉ được chọn một mục tiêu";

            switch (type) {
                case "soi" -> {
                    if (!isLivingWolf(actor)) return "Chỉ Sói còn sống mới được cắn";
                    if (actor.isDisabledSkill()) return "Phe Sói đang mất lượt cắn";
                    // Kẻ phản bội không bị chặn: sói không biết họ là ai nên vẫn có thể cắn nhầm.
                    if (isLivingWolf(target)) return "Phe Sói không thể cắn đồng đội Sói";
                    if (hasWolfBite) return "Phe Sói chỉ được cắn một mục tiêu";
                    hasWolfBite = true;
                }
                case "recruit" -> {
                    if (!curseAvailable) return "Lời nguyền đã được sử dụng";
                    if (actor.getId() != ROLE_CURSED_WOLF) return "Chỉ Sói nguyền được sử dụng lời nguyền";
                    if (actor.isDisabledSkill()) return "Sói nguyền đang mất chức năng";
                    if (actor == target) return "Sói nguyền không thể tự nguyền mình";
                    if (ID_SOI.contains(target.getId())) return "Không thể nguyền người thuộc phe Sói";
                    if (hasCurse) return "Sói nguyền chỉ được chọn một mục tiêu";
                    hasCurse = true;
                }
                case "inspect" -> {
                    if (!actor.isInspectSkill()) return "Người thực hiện không có chức năng Tiên tri";
                    if (actor.isDisabledSkill()) return "Tiên tri đang mất chức năng";
                    if (actor == target) return "Tiên tri không thể tự soi mình";
                }
                case "kill" -> {
                    if (actor.getKillSkill() <= 0) return actor.getRole() + " đã hết quyền giết";
                    if (actor.isDisabledSkill()) return actor.getRole() + " đang mất chức năng";
                }
                case "prot" -> {
                    if (actor.getProtectedSkill() <= 0) return actor.getRole() + " đã hết quyền bảo vệ";
                    if (actor.isDisabledSkill()) return actor.getRole() + " đang mất chức năng";
                    if (actor.getId() == ROLE_WITCH
                            && !Objects.equals(target.getIpData(), wolfBiteTarget)) {
                        return "Phù thủy chỉ được cứu người bị Sói cắn trong đêm nay";
                    }
                }
                case "conn" -> {
                    if (actor.getConnectSkill() <= 0) return actor.getRole() + " đã hết chức năng";
                    if (actor.isDisabledSkill()) return actor.getRole() + " đang mất chức năng";
                    if (actor.getId() == ROLE_CLONE) {
                        if (actor.getConnectSkill() != 2) return "Nhân bản đã chọn mục tiêu trước đó";
                        if (actor == target) return "Nhân bản không thể tự chọn mình";
                    }
                    if (actor.getId() == ROLE_SILENCER) {
                        if (actor == target) return "Câm lặng không thể tự chọn mình";
                        if (target.getId() == ROLE_ANGEL) return "Thiên thần không thể bị câm lặng";
                    }
                    if (actor.getId() == ROLE_OLD_WITCH) {
                        return "Phù thủy già chỉ hành động trong pha ban ngày";
                    }
                }
                default -> { return "Loại hành động không hợp lệ"; }
            }
        }

        if (hasWolfBite && hasCurse) return "Nguyền thay cho lượt cắn, không thể thực hiện cả hai trong cùng đêm";
        return null;
    }

    private String resolveNight(Map<String, String> toKill, Map<String, DataMember> playerByDevice,
                                Map<String, String> toSave, List<String> adminEvents,
                                List<String> playerEvents) {
        toSave.forEach((device, reason) -> {
            DataMember target = playerByDevice.get(device);
            if (target != null) adminEvents.add(displayName(target) + ": " + target.getRole() + " " + reason);
        });

        toKill.forEach((device, reason) -> {
            DataMember target = playerByDevice.get(device);
            if (target == null || target.isDead()) return;

            markDead(target);
            String event = displayName(target) + ": " + target.getRole() + " " + reason;
            adminEvents.add(event);
            playerEvents.add(event);
            applyDeathEffects(device, target, playerByDevice, adminEvents);
        });

        if (toKill.isEmpty()) {
            String noDeath = "Không có ai bị loại trong đêm nay";
            adminEvents.add(noDeath);
            playerEvents.add(noDeath);
        }

        String label = "Đêm " + countNight++;
        adminCurrentHistory.add(new RoundHistory(label, adminEvents));
        playerCurrentHistory.add(new RoundHistory(label, playerEvents));
        dayPhaseActive = true;
        return "Detail: " + System.lineSeparator() + String.join(System.lineSeparator(), adminEvents);
    }

    private void applyDeathEffects(String device, DataMember deadPlayer,
                                   Map<String, DataMember> playerByDevice, List<String> adminEvents) {
        List<String> linkedDevices = linkRole.remove(device);
        if (linkedDevices != null) {
            for (String linkedDevice : linkedDevices) {
                DataMember clone = playerByDevice.get(linkedDevice);
                if (clone == null || clone.isDead()) continue;

                String copiedRole = roleNameForCopy(deadPlayer);
                copyRoleCapabilities(clone, deadPlayer);
                adminEvents.add("Nhân bản " + displayName(clone) + " đã trở thành " + copiedRole);
            }
        }

        if (deadPlayer.getId() == ROLE_SEER) {
            pls.stream()
                    .filter(player -> player.getId() == ROLE_APPRENTICE_SEER && !player.isDead())
                    .findFirst()
                    .ifPresent(apprentice -> {
                        copyRoleCapabilities(apprentice, deadPlayer);
                        adminEvents.add("Tiên tri tập sự " + displayName(apprentice) + " đã trở thành Tiên tri");
                    });
        }
    }

    private void copyRoleCapabilities(DataMember recipient, DataMember source) {
        String copiedRole = roleNameForCopy(source);
        recipient.setId(source.getId());
        recipient.setRole(copiedRole);
        recipient.setDetailShow(copiedRole);
        recipient.setDescription(source.getDescription());
        recipient.setKillSkill(source.getKillSkill());
        recipient.setProtectedSkill(source.getProtectedSkill());
        recipient.setSuperProtectedSkill(source.isSuperProtectedSkill());
        recipient.setConnectSkill(source.getConnectSkill());
        recipient.setInspectSkill(source.isInspectSkill());
        recipient.setDisabledSkill(false);
    }

    private void convertToBasicWolf(DataMember target) {
        DataMember basicWolf = datas.stream()
                .filter(role -> role.getId() == ROLE_WOLF)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy role Sói"));
        copyRoleCapabilities(target, basicWolf);
    }

    private boolean inspectTarget(DataMember target) {
        if (target.getId() == ROLE_DOUBLE_CHECK_WOLF) {
            int inspections = seerInspectionCounts.merge(target.getIpData(), 1, Integer::sum);
            return inspections >= 2;
        }
        return (target.getId() >= ROLE_WOLF && target.getId() < ROLE_TRAITOR)
                || target.getId() == ROLE_ASSASSIN;
    }

    private boolean isLivingWolf(DataMember player) {
        return player != null && !player.isDead() && ID_SOI.contains(player.getId())
                && player.getId() != ROLE_TRAITOR;
    }

    private Map<String, DataMember> playersByDevice() {
        return pls.stream()
                .filter(player -> !ObjectUtils.isEmpty(player.getIpData()))
                .collect(Collectors.toMap(DataMember::getIpData, player -> player,
                        (first, ignored) -> first, LinkedHashMap::new));
    }

    private void markDead(DataMember player) {
        if (player.isDead()) return;
        player.setDead(true);
        deadPls.add(player);
    }

    private String displayName(DataMember player) {
        return ObjectUtils.isEmpty(player.getNameMember()) ? "Ẩn danh" : player.getNameMember();
    }

    /**
     * Tên người chơi từng xuất hiện trong lịch sử: ván hiện tại (còn sống lẫn đã chết)
     * và mọi ván đã lưu. Giao diện dùng danh sách này để tô đậm tên trong dòng sự kiện.
     * Người chưa đặt tên bị bỏ qua để chữ "Ẩn danh" không bị tô nhầm.
     */
    public synchronized List<String> getAllPlayerNames() {
        Set<String> names = new LinkedHashSet<>();
        collectNames(names, pls);
        collectNames(names, deadPls);
        detailEachGame.values().forEach(players -> collectNames(names, players));
        return new ArrayList<>(names);
    }

    private void collectNames(Set<String> names, List<DataMember> players) {
        players.stream()
                .map(DataMember::getNameMember)
                .filter(name -> !ObjectUtils.isEmpty(name))
                .forEach(names::add);
    }

    private String roleNameForCopy(DataMember player) {
        return ObjectUtils.isEmpty(player.getDetailShow()) ? player.getRole() : player.getDetailShow();
    }

    /**
     * Câu mô tả một lượt bảo vệ, ghép sau "Tên: Vai trò ".
     * Tránh lối nói lặp "Bảo vệ được Bảo vệ bảo vệ", và nói rõ khi tự cứu mình.
     */
    private String protectionReason(DataMember actor, DataMember target) {
        boolean witch = actor.getId() == ROLE_WITCH;
        if (actor == target) return witch ? "tự cứu chính mình" : "tự bảo vệ chính mình";
        return witch ? "được Phù thủy cứu" : "được " + actor.getRole() + " che chở";
    }

    private String joinReasons(String first, String second) {
        if (ObjectUtils.isEmpty(first)) return second;
        if (ObjectUtils.isEmpty(second) || first.contains(second)) return first;
        return first + " và " + second;
    }

    private void addUnique(List<String> events, String event) {
        if (!events.contains(event)) events.add(event);
    }

    private void rememberSession(Set<Long> sessions, long sessionId) {
        sessions.add(sessionId);
        while (sessions.size() > 100) {
            Iterator<Long> iterator = sessions.iterator();
            if (!iterator.hasNext()) return;
            iterator.next();
            iterator.remove();
        }
    }

    private void resetCurrentGameState() {
        pls.clear();
        deadPls.clear();
        listShowForMember.clear();
        playerCurrentHistory.clear();
        adminCurrentHistory.clear();
        seerInspectionCounts.clear();
        linkRole.clear();
        curseAvailable = false;
        dayPhaseActive = false;
        oldWitchLastActionDay = 0;
        allowDeadViewGameHistory = false;
        allowShowAliveDead = false;
        dayIsReadyKill = false;
        countNight = 1;
    }

    private List<DataMember> snapshotPlayers(List<DataMember> players) {
        return players.stream().map(player -> DataMember.builder()
                .id(player.getId())
                .role(player.getRole())
                .ipData(player.getIpData())
                .description(player.getDescription())
                .total(player.getTotal())
                .nameMember(player.getNameMember())
                .idPlayGame(player.getIdPlayGame())
                .protectedSkill(player.getProtectedSkill())
                .killSkill(player.getKillSkill())
                .superProtectedSkill(player.isSuperProtectedSkill())
                .disabledSkill(player.isDisabledSkill())
                .isDead(player.isDead())
                .inspectSkill(player.isInspectSkill())
                .detailShow(player.getDetailShow())
                .lifeLink(player.getLifeLink())
                .lifeLinkIds(player.getLifeLinkIds() == null ? null : List.copyOf(player.getLifeLinkIds()))
                .connectSkill(player.getConnectSkill())
                .build()).toList();
    }

    private List<Map<String, Object>> buildAdminArchivedGames() {
        return adminArchivedHistory.entrySet().stream()
                .sorted(Map.Entry.<Integer, List<RoundHistory>>comparingByKey().reversed())
                .map(entry -> {
                    Map<String, Object> game = new LinkedHashMap<>();
                    game.put("gameNumber", entry.getKey());
                    game.put("rounds", entry.getValue());
                    List<Map<String, String>> players = detailEachGame.getOrDefault(entry.getKey(), List.of()).stream()
                            .map(player -> Map.of(
                                    "name", displayName(player),
                                    "role", player.getRole().replaceAll("<[^>]*>", "")))
                            .toList();
                    game.put("players", players);
                    return game;
                })
                .toList();
    }

    private DataMember createDM(DataMember d, Integer total) {
        return DataMember.builder().id(d.getId()).role(d.getRole()).description(d.getDescription()).total(String.valueOf(total)).build();
    }

    public void createTest(int totalTest) {
        String[] FIRST_NAMES = {
                "An", "Bình", "Chi", "Dũng", "Hà",
                "Hải", "Hùng", "Lan", "Linh", "Mai",
                "Minh", "Nam", "Ngọc", "Phong", "Quân",
                "Sơn", "Thảo", "Trang", "Tuấn", "Việt",
                "Sơn", "Thảo", "Trang", "Tuấn", "Việt"
        };
        try {
            for (int i = 0; i < totalTest; i++) {
                DataMember data = pls.get(i);
                data.setIpData(UUID.randomUUID().toString());
                data.setNameMember(FIRST_NAMES[i] + " " + (i +1) );
            }
        } catch (Exception ignored) {
            System.out.println(ignored.getMessage());
        }

    }
}
