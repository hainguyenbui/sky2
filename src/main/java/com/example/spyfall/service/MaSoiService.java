package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.GameSetupRequest;
import com.example.spyfall.common.NightActionDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class MaSoiService {

    public static final List<Integer> WOLF_ROLE_IDS = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    public static final List<Integer> OUTSIDER_ROLE_IDS = List.of(21, 20, 22);

    private static final String IMAGE_PATH = "/qrcode.png";
    private static final List<String> DAY_DEATH_REASONS = List.of(
            " bị thủ tiêu vì biết quá nhiều", " không muốn chơi nữa", " bị thù ghét", " nói quá nhiều");
    private static final List<String> LOVE_DEATH_REASONS = List.of(
            " dã đi là đi chung", " một bước cũng không lệch", " kè kè bên cạnh", " đi cùng cho vui", " khóc hết nước mắt", " ăn chơi xa đọa");

    private final DataInputService dataInputService;
    private final List<DataMember> roleCatalog = new ArrayList<>();
    private final List<DataMember> players = new ArrayList<>();
    private final List<DataMember> deadPlayers = new ArrayList<>();
    private final List<DataMember> visibleRoles = new ArrayList<>();
    private final Map<String, List<String>> copyLinks = new HashMap<>();
    private final List<String> adminHistory = new ArrayList<>();
    private final Map<Integer, List<DataMember>> completedGames = new TreeMap<>(Comparator.reverseOrder());
    private final Map<Integer, Map<String, String>> completedGameDetails = new LinkedHashMap<>();
    @Getter
    private final Map<String, String> currentGameHistory = new LinkedHashMap<>();

    @Getter
    @Setter
    private boolean gameEnded = true;
    private boolean wolfWasRemovedDuringSetup;
    private boolean curseAvailable;
    @Getter
    @Setter
    private boolean allowDeadViewGameHistory = true;
    @Getter
    @Setter
    private boolean allowShowAliveDead = true;
    @Getter
    @Setter
    private boolean invertedSeer = true;
    @Getter
    private boolean dayKillProcessed;
    private int nightNumber = 1;
    @Getter
    private String nightDetails = "";
    @Getter
    private String dayDetails = "";
    private boolean sickWolfWasTargeted = false;
    @Getter
    Map<String, Integer> deviceIdOrder = new HashMap<>();
    private final List<String> loveLinks = new ArrayList<>();

    public MaSoiService(DataInputService dataInputService) {
        this.dataInputService = dataInputService;
    }

    public String getImage() {
        return IMAGE_PATH;
    }

    public List<DataMember> getDatas() throws Exception {
        dataInputService.prepareDataMaSoi(roleCatalog);
        return roleCatalog;
    }

    public List<DataMember> getPls() {
        return players;
    }

    public List<DataMember> getDeadPls() {
        return deadPlayers;
    }

    public List<DataMember> getListShowForMember() {
        return visibleRoles;
    }

    public List<String> getHistoryAdmin() {
        return adminHistory;
    }

    public int getGameNumber() {
        return completedGames.size() + 1;
    }

    public boolean isShowSoiNguyen() {
        return curseAvailable;
    }

    public String loadGame(Map<String, String> params) throws Exception {
        return loadGame(GameSetupRequest.fromQueryParams(params));
    }

    public String loadGame(GameSetupRequest setup) throws Exception {
        if (!gameEnded) {
            return "ALERT GAME NOT END YES";
        }
        if (setup.getTotalPlayers() == 0) {
            return "ERROR LỖI KHÔNG TỔNG NGƯỜI";
        }

        resetGame(setup);
        createPlayers(setup);
        removeExtraRoles(setup.getTotalPlayers());
        applyOptionalSetupRules(setup);
        return "OK load";
    }

    private void resetGame(GameSetupRequest setup) throws Exception {
        gameEnded = false;
        dataInputService.prepareDataMaSoi(roleCatalog);
        players.clear();
        deadPlayers.clear();
        visibleRoles.clear();
        copyLinks.clear();
        adminHistory.clear();
        currentGameHistory.clear();
        wolfWasRemovedDuringSetup = false;
        curseAvailable = false;
        dayKillProcessed = false;
        nightNumber = 1;
        nightDetails = "";
        dayDetails = "";
        sickWolfWasTargeted = false;
    }

    private void createPlayers(GameSetupRequest setup) {
        for (DataMember role : roleCatalog) {
            int count = setup.getRoleCount(role.getId());
            if (count > 0) {
                visibleRoles.add(roleSummary(role, count));
            }
            for (int index = 0; index < count; index++) {
                players.add(createPlayer(role));
            }
        }
    }

    private DataMember createPlayer(DataMember role) {
        return DataMember.builder()
                .id(role.getId())
                .idPlayGame(String.valueOf(players.size() + 1))
                .role(role.getRole())
                .description(role.getDescription())
                .killSkill(role.getKillSkill())
                .protectedSkill(role.getProtectedSkill())
                .connectSkill(role.getConnectSkill())
                .superProtectedSkill(role.isSuperProtectedSkill())
                .roleShow(role.getRole())
                .roleDefault(role.getRole())
                .idDefault(role.getId())
                .orderCall(role.getOrderCall())
                .disabledSkill(role.isDisabledSkill())
                .build();
    }

    private void removeExtraRoles(int requestedPlayerCount) {
        // neu so nguoi choi it hon so nhan vat
        Random random = new Random();
        while (players.size() > requestedPlayerCount) {
            DataMember removed = players.remove(random.nextInt(players.size()));
            wolfWasRemovedDuringSetup |= isWolf(removed);
        }
    }

    private void applyOptionalSetupRules(GameSetupRequest setup) {
        curseAvailable = players.stream().anyMatch(player -> player.getId() == 3);
        if (setup.isSoiNguyenEnabled() || wolfWasRemovedDuringSetup) {
            players.stream().filter(this::isWolfAction).forEach(this::addWolfCurse);
        }
        if (setup.isCupidEnabled() && players.size() >= 2) {
            linkCupidPair();
        }
    }

    private void addWolfCurse(DataMember player) {
        player.setRole(player.getRole() + " + Sói Nguyền");
        player.setDescription(player.getDescription() + "sói nguyền");
        curseAvailable = true;
    }

    private void linkCupidPair() {
        Collections.shuffle(players);
        DataMember first = players.get(0);
        DataMember second = players.get(1);
        first.setRole(first.getRole() + " cặp đôi với " + second.getRoleDefault());
        first.setLifeLink(1);
        first.setLifeLinkIds(List.of(second.getId()));
        second.setRole(second.getRole() + " cặp đôi với " + first.getRoleDefault());
        second.setLifeLink(1);
        second.setLifeLinkIds(List.of(first.getId()));
    }

    public DataMember getOrAssignRole(String deviceId, String playerName) {
        if (players.isEmpty()) {
            return null;
        }
        Optional<DataMember> existingPlayer = players.stream()
                .filter(player -> Objects.equals(deviceId, player.getIpData()))
                .findFirst();
        if (existingPlayer.isPresent()) {
            DataMember player = existingPlayer.get();
            if (playerName != null) {
                player.setNameMember(playerName);
            }
            updateTraitorDetails(player);
            return player;
        }

        Collections.shuffle(players);
        players.sort(Comparator.comparing(DataMember::getIpData, Comparator.nullsFirst(String::compareTo)));
        DataMember availablePlayer = players.get(0);
        if (availablePlayer.getIpData() != null) {
            return new DataMember();
        }
        availablePlayer.setIpData(deviceId);
        availablePlayer.setNameMember(playerName);
        deviceIdOrder.put(deviceId, deviceIdOrder.size() + 1);
        return availablePlayer;
    }

    private void updateTraitorDetails(DataMember player) {
        if (player.getId() == 15) {
            String wolves = players.stream()
                    .filter(candidate -> candidate.getId() < 15)
                    .map(DataMember::getNameMember)
                    .collect(Collectors.joining(", "));
            player.setDescription("Sói là: " + wolves);
        }
    }

    public Map<String, Object> getGameHistoryData() {
        List<Map<String, Object>> games = new ArrayList<>();
        completedGames.forEach((gameNumber, gamePlayers) -> {
            List<Map<String, Object>> playerViews = gamePlayers.stream()
                    .sorted(Comparator.comparing(DataMember::getId))
                    .map(this::historyPlayerView)
                    .toList();
            Map<String, Object> game = new HashMap<>();
            game.put("gameNumber", gameNumber);
            game.put("details", completedGameDetails.get(gameNumber));
            game.put("players", playerViews);
            games.add(game);
        });
        return Map.of("games", games);
    }

    private Map<String, Object> historyPlayerView(DataMember player) {
        String name = player.getNameMember() != null ? player.getNameMember()
                : player.getIpData() != null ? "Ẩn danh" : "Chưa nhận";
        return Map.of(
                "id", player.getId(),
                "displayName", name,
                "displayLocation", removeHtml(player.getRole()),
                "isSoi", isWolf(player),
                "isOutsider", OUTSIDER_ROLE_IDS.contains(player.getId())
        );
    }

    public String endGame() {
        if (nightNumber > 1) {
            int gameNumber = completedGames.size() + 1;
            completedGames.put(gameNumber, new ArrayList<>(players));
            completedGameDetails.put(gameNumber, new LinkedHashMap<>(currentGameHistory));
        }
        gameEnded = true;
        players.clear();
        visibleRoles.clear();
        copyLinks.clear();
        dayKillProcessed = false;
        nightNumber = 1;
        currentGameHistory.clear();
        return "End Game : " + completedGames.size();
    }

    public String getActiveRolesString() {
        Set<String> activeRoles = new LinkedHashSet<>();
        List<Integer> hiddenRoleIds = List.of(21, 30, 35, 41);
        players.stream().sorted(Comparator.comparing(DataMember::getId)).forEach(player -> {
            if (player.getId() < 15) {
                activeRoles.add("Soi");
            } else if (!hiddenRoleIds.contains(player.getId())) {
                activeRoles.add(player.getRole());
            }
        });
        return String.join(", ", activeRoles);
    }

    public String processNight(List<NightActionDto> actions) {
        dayKillProcessed = false;
        Map<String, DataMember> playersByDevice = playersByDevice();
        NightResolution resolution = new NightResolution();
        List<NightActionDto> submittedActions = actions == null ? List.of() : actions;

        resetNightSkillBlocks();// enable full skill khi bị disable đêm hôm trước TODO nên hiện lịch xử cắn hụt chỗ này
        adminHistory.clear();
        evaluatePreNightEffects(submittedActions, playersByDevice, resolution);
        applyConnectionForSeer(submittedActions, playersByDevice, resolution);
        evaluateNightActions(submittedActions, playersByDevice, resolution);
        resolveNightDeaths(playersByDevice, resolution);
        return publishNightResult(playersByDevice, resolution);
    }

    private void evaluatePreNightEffects(List<NightActionDto> actions, Map<String, DataMember> playersByDevice,
                                         NightResolution resolution) {
        Set<String> wolfDevices = livingWolves().stream().map(DataMember::getIpData).collect(Collectors.toSet());
        String alphaWolfDevice = alphaWolfDevice();
        for (NightActionDto action : actions) {
            if (action.getRoleId() == 38) {
                applySilence(action, wolfDevices, alphaWolfDevice, resolution);
            } else if (action.getRoleId() == 42) {
                registerCopyLink(action, playersByDevice, resolution);
            } else if (action.getRoleId() == 44 && "ON".equals(action.getConnValue())) {
                resolution.superProtectionActive = true;
                resolution.events.add("Thiên thần đã bảo vệ lượt này");
                findPlayerByRole(44).ifPresent(player -> player.setSuperProtectedSkill(false));
            } else {
                applySicknessPenalty(action, playersByDevice, resolution);
            }
        }
    }

    private void evaluateNightActions(List<NightActionDto> actions, Map<String, DataMember> playersByDevice,
                                      NightResolution resolution) {
        DataMember sickWolf = findPlayerByRole(5).orElse(null);
        for (NightActionDto action : actions) {
            applySickWolfRetaliation(action, sickWolf, resolution);
            switch (action.actionType()) {
                case SOI -> applyWolfBite(action, playersByDevice, resolution);
                case KILL -> applyKillAction(action, playersByDevice, resolution);
                case PROTECT -> applyProtection(action, playersByDevice, resolution);
                case CONNECT -> applyConnection(action, playersByDevice, resolution);
                case RECRUIT -> applyRecruitment(action, playersByDevice, resolution);
                default -> { }
            }
        }
        if (resolution.sickWolfWasTargeted) {
            sickWolfWasTargeted = true; // Nếu sói si đa đã xử lý trong vòng thì lưu lại
        }
    }

    private void applySilence(NightActionDto action, Set<String> wolfDevices, String alphaWolfDevice,
                              NightResolution resolution) {
        String message = action.getTargetName() + ": " + action.getTargetRoleName() + " bị câm lặng";
        if (!wolfDevices.contains(action.getTargetDeviceId())) {
            resolution.disabledRoles.put(action.getTargetDeviceId(), message);
        } else {
            if (Objects.equals(action.getTargetDeviceId(), alphaWolfDevice)) {
                resolution.disabledRoles.put("soi", message);
            }
            if (action.getTargetRoleId() == 5) {
                resolution.disabledRoles.put(action.getTargetDeviceId(), message);
            } else if (action.getTargetRoleId() == 3) {
                resolution.disabledRoles.put("soiNguyen", message);
            }
        }
    }

    private void registerCopyLink(NightActionDto action, Map<String, DataMember> playersByDevice,
                                  NightResolution resolution) {
        DataMember copycat = playersByDevice.get(action.getDeviceId());
        if (copycat == null) {
            return;
        }
        copycat.setDisabledSkill(true);
        copycat.setOldTargetId(action.getTargetDeviceId());
        copyLinks.put(action.getTargetDeviceId(), List.of(copycat.getIpData()));
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        resolution.events.add("Nhân bản đã chọn " + target.getNameMember() + ": " + target.getRoleDefault());
    }

    private void applySicknessPenalty(NightActionDto action, Map<String, DataMember> playersByDevice,
                                      NightResolution resolution) {
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (target == null || target.getId() != 41 || resolution.superProtectionActive) {
            return;
        }
        if (action.getRoleId() == 1) {
            livingWolves().forEach(player -> player.setDisabledSkill(true));
            resolution.nextDayBlocks.put("Soi", "Sói cạp trúng người bệnh đêm sau bị mất lượt");
        } else if (action.getRoleId() == 20) {
            DataMember attacker = playersByDevice.get(action.getDeviceId());
            if (attacker != null) {
                attacker.setDisabledSkill(true);
                resolution.nextDayBlocks.put("Sát thủ", "Sát thủ cạp trúng người bệnh đêm sau bị mất lượt");
            }
        }
    }

    private void applySickWolfRetaliation(NightActionDto action, DataMember sickWolf, NightResolution resolution) {
        if (sickWolf == null || sickWolfWasTargeted
                || !Objects.equals(sickWolf.getIpData(), action.getTargetDeviceId())) {
            return;
        }
        if (!resolution.disabledRoles.containsKey(action.getTargetDeviceId())) {
            addReason(resolution.deaths, action.getDeviceId(), "đụng phải Sói bị Sida");
        }
        resolution.sickWolfWasTargeted = true;
    }

    private void applyWolfBite(NightActionDto action, Map<String, DataMember> playersByDevice,
                               NightResolution resolution) {
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (resolution.disabledRoles.containsKey("soi")) {
            resolution.events.add("Sói bị bẻ răng khi cắn " + target.getNameMember() + ": " + target.getRole());
            return;
        }
        if (target != null && target.getId() == 40 && !resolution.disabledRoles.containsKey(action.getTargetDeviceId())) {
            target.setId(1);
            target.setRole(target.getRole() + " + bạn đã là Sói");
            resolution.events.add(action.getTargetName() + ": " + action.getTargetRoleName() + " bị cắn và đã trở thành Sói");
        } else if (target != null && target.getId() == 20) {
            resolution.events.add("Sói cắn hụt sát thủ");
        } else {
            addReason(resolution.deaths, action.getTargetDeviceId(), "bị Soi Cắn");
        }
    }

    private void applyKillAction(NightActionDto action, Map<String, DataMember> playersByDevice,
                                 NightResolution resolution) {
        DataMember attacker = playersByDevice.get(action.getDeviceId());
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (attacker == null || target == null) {
            return;
        }
        attacker.decreaseKillSkill();
        if (resolution.disabledRoles.containsKey(action.getDeviceId())) {
            resolution.events.add(attacker.getRoleDefault() + " bị câm lặng nên không thể tác động " + target.getRoleDefault());
            return;
        }
        if (action.getRoleId() == 31) {
            addReason(resolution.unpreventableDeaths, action.getTargetDeviceId(), " bị " + attacker.getRoleDefault() + " quăng bình");
        } else if (action.getRoleId() == 43) {
            if (action.getTargetRoleId() >= 30) {
                addReason(resolution.deaths, action.getDeviceId(), " loại bỏ nhầm dân nên đi theo");
            }
            addReason(resolution.deaths, action.getTargetDeviceId(), " bị " + action.getRoleName() + " tác động vật lý");
        } else if (action.getRoleId() == 46 ) {
            if (Objects.equals(attacker.getOldTargetId(), action.getTargetDeviceId())) {
                addReason(resolution.deaths, action.getTargetDeviceId(), " bị " + action.getRoleName() + " tác động vật lý");
                if (action.getTargetRoleId() < 30) {
                    attacker.setKillSkill(1);
                } else {
                    resolution.events.add(attacker.getNameMember() + " đã mất boom");
                }
            } else {
                attacker.setOldTargetId(action.getTargetDeviceId());
                attacker.setKillSkill(1);
                adminHistory.add(target.getNameMember() + ": " + target.getRoleDefault() + " đang cầm boooom");
                resolution.events.add(attacker.getNameMember() + ": " + attacker.getRoleDefault() + " đã quăng bom cho " + target.getNameMember());
            }
        } else {
            addReason(resolution.deaths, action.getTargetDeviceId(), " bị " + action.getRoleName() + " tác động vật lý");
        }
    }

    private void applyProtection(NightActionDto action, Map<String, DataMember> playersByDevice,
                                 NightResolution resolution) {
        DataMember protector = playersByDevice.get(action.getDeviceId());
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (protector == null) {
            return;
        }
        protector.decreaseProtectedSkill();
        if (!resolution.disabledRoles.containsKey(action.getDeviceId())) {
            addReason(resolution.savedPlayers, action.getTargetDeviceId(), "được " + action.getRoleName() + " bảo vệ");
        } else {
            resolution.events.add(protector.getRoleDefault() + " bị câm lặng nên không thể bảo vệ " + target.getNameMember() + ": " + target.getRoleDefault());
        }
        if (protector.getId() == 31) {
            return;
        }
        protector.setOldTargetId(action.getTargetDeviceId());
    }

    private void applyConnection(NightActionDto action, Map<String, DataMember> playersByDevice,
                                 NightResolution resolution) {
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (resolution.disabledRoles.containsKey(action.getDeviceId())) {
            if (action.getRoleId() == 34) {
                resolution.events.add("Thợ săn không thể ghim " + target.getNameMember() + ": " + target.getRoleDefault() + " vì bị câm lặng");
            } else if (action.getRoleId() == 45) {
                resolution.events.add("Phù Thủy Già không thể đuổi " + target.getNameMember() + ": " + target.getRoleDefault() + " vì bị câm lặng");
            }
            return;
        }
        if (action.getRoleId() == 34) {
            resolution.hunterLinks.put(action.getDeviceId(), action.getTargetDeviceId());
        } else if (action.getRoleId() == 45) {
            adminHistory.add("Phù Thủy Già đuổi " + action.getTargetName() + " ra khòi làng");
        }
    }

    private void applyConnectionForSeer(List<NightActionDto> actions, Map<String, DataMember> playersByDevice,
                                 NightResolution resolution) {
        actions.forEach(action -> {
            if (action.getRoleId() == 32) {
                DataMember attacker = playersByDevice.get(action.getDeviceId());
                DataMember target = playersByDevice.get(action.getTargetDeviceId());
                if (resolution.disabledRoles.containsKey(action.getDeviceId())) {
                    String result = (WOLF_ROLE_IDS.contains(target.getId()) && target.getId() != 15)
                            || target.getId() == 20
                            ? "Sói/ Sát thủ"
                            : "Dân";

                    if (invertedSeer) {
                        result = "Dân".equals(result) ? "Sói/ Sát thủ" : "Dân";
                    }
                    resolution.events.add(attacker.getRoleDefault() + " soi " + target.getNameMember() + ": " + target.getRole() + " kết quả là " + result);
                    return;
                }

                String result;
                if ((WOLF_ROLE_IDS.contains(target.getId()) && target.getId() != 15) || target.getId() == 20) {
                    result = "Sói/ Sát thủ";
                } else {
                    result = "Dân";
                }
                resolution.events.add(attacker.getRoleDefault() + " soi " + target.getNameMember() + ": " + target.getRole() + " kết quả là " + result);
            }
        });
    }

    private void applyRecruitment(NightActionDto action, Map<String, DataMember> playersByDevice,
                                  NightResolution resolution) {
        DataMember target = playersByDevice.get(action.getTargetDeviceId());
        if (target == null) {
            return;
        }
        if (resolution.disabledRoles.containsKey("soiNguyen")) {
            curseAvailable = false;
            resolution.events.add("Sói bị câm lặng nên không thể nguyền " + target.getNameMember() + ": " + target.getRoleDefault());
            resolution.events.add("Sói không thể nguyền thêm ai nữa");
            return;
        }
        curseAvailable = false;
        target.setId(1);
        target.setRole(target.getRole() + " - Soi");
        target.setDisabledSkill(false);
        target.setKillSkill(9999);
        target.setOldTargetId(null);
        String message = target.getNameMember() + ": " + target.getRoleDefault() + " bị nguyền thành Sói";
        adminHistory.add(message);
        resolution.events.add(message);
    }

    private void resolveNightDeaths(Map<String, DataMember> playersByDevice, NightResolution resolution) {
        Map<String, String> deathReasons = new LinkedHashMap<>(resolution.deaths);
        resolution.savedPlayers.keySet().forEach(resolution.deaths::remove);
        resolution.unpreventableDeaths.forEach((deviceId, reason) -> addReason(resolution.deaths, deviceId, reason));
        // thêm vào cái cho cặp đôi khi ra đi buổi sáng
        loveLinks.forEach(partnerId -> addReason(resolution.deaths, partnerId, LOVE_DEATH_REASONS.get(new Random().nextInt(LOVE_DEATH_REASONS.size()))));
        loveLinks.clear();

        expandLinkedDeaths(playersByDevice, resolution);
        deathReasons.putAll(resolution.deaths);
        // Lưu toàn bộ lịch su
        deathReasons.forEach((deviceId, reason) -> {
            DataMember player = playersByDevice.get(deviceId);
            if (player == null) {
                return;
            }
            resolution.events.add(player.getNameMember() + ": " + player.getRoleDefault() + " " + reason);
        });
        if (resolution.superProtectionActive) {
            resolution.deaths.clear();
        }
    }

    private void expandLinkedDeaths(Map<String, DataMember> playersByDevice, NightResolution resolution) {
        int previousDeathCount;
        do {
            previousDeathCount = resolution.deaths.size();
            resolution.hunterLinks.forEach((hunterDevice, targetDevice) -> {
                if (resolution.deaths.containsKey(hunterDevice)) {
                    addReason(resolution.deaths, targetDevice, "bị thợ săn ghim");
                }
            });
            for (String deviceId : new ArrayList<>(resolution.deaths.keySet())) {
                DataMember player = playersByDevice.get(deviceId);
                if (player.getLifeLink() > 0) {
                    findCupidPartner(player).ifPresent(partner -> addReason(resolution.deaths, partner.getIpData(), "liên kết sinh mệnh với " + player.getNameMember()));
                }
            }
        } while (previousDeathCount != resolution.deaths.size());
    }

    private String publishNightResult(Map<String, DataMember> playersByDevice, NightResolution resolution) {
        StringBuilder story = new StringBuilder();
        resolution.events.forEach(message -> appendStory(story, message));
        resolution.disabledRoles.values().forEach(message -> appendStory(story, message));
        resolution.savedPlayers.forEach((deviceId, reason) -> {
            DataMember player = playersByDevice.get(deviceId);
            if (player != null) {
                appendStory(story, player.getNameMember() + ": " + player.getRole() + " " + reason);
            }
        });
        resolution.deaths.forEach((deviceId, reason) -> {
            DataMember player = playersByDevice.get(deviceId);
            if (player == null) {
                return;
            }
//            appendStory(story, player.getNameMember() + ": " + player.getRole() + " " + reason);
            eliminatePlayer(player);
            adminHistory.add(player.getNameMember() + " bị loại");
            applyDeathConsequences(deviceId, player, playersByDevice, message -> {
                adminHistory.add(message);
                appendStory(story, message);
            });
        });
        resolution.nextDayBlocks.values().forEach(message -> appendStory(story, message));
        nightDetails = story.toString();
        currentGameHistory.put("Đêm " + nightNumber++, nightDetails);
        if (resolution.deaths.isEmpty()) {
            adminHistory.add("Không có ai bị giết đêm nay");
        }
        return "Detail: " + adminHistory.stream()
                .map(message -> System.lineSeparator() + message)
                .collect(Collectors.joining());
    }

    public String processDay(List<String> deviceIds) {
//        dayKillProcessed = true;
        Map<String, DataMember> playersByDevice = playersByDevice();
        Map<String, DataMember> eliminatedPlayers = new LinkedHashMap<>();
        StringBuilder story = new StringBuilder();

        for (String deviceId : deviceIds == null ? List.<String>of() : deviceIds) {
            DataMember player = playersByDevice.get(deviceId);
            if (player == null) {
                continue;
            }
            eliminateDuringDay(player, eliminatedPlayers, story, true);
            eliminateCupidPartner(player, eliminatedPlayers, story);
        }
        eliminatedPlayers.forEach((deviceId, player) -> applyDeathConsequences(deviceId, player, playersByDevice,
                message -> story.append(message).append("<br>")));
        if (!story.isEmpty()) {
            dayDetails = story.toString();
            String keyDay = "Ngày " + nightNumber;
            if (currentGameHistory.containsKey(keyDay)) {
                String value = currentGameHistory.get(keyDay);
                dayDetails += value;
            }
            currentGameHistory.put(keyDay, dayDetails);
        }
        return "Detail: " + story;
    }

    private void eliminateDuringDay(DataMember player, Map<String, DataMember> eliminatedPlayers,
                                    StringBuilder story, boolean includeReason) {
        eliminatePlayer(player);
        eliminatedPlayers.put(player.getIpData(), player);
        story.append(player.getNameMember()).append(": ").append(player.getRole());
        if (includeReason) {
            story.append(DAY_DEATH_REASONS.get(new Random().nextInt(DAY_DEATH_REASONS.size())));
        }
        story.append("<br>");
    }

    private void eliminateCupidPartner(DataMember player, Map<String, DataMember> eliminatedPlayers, StringBuilder story) {
        if (player.getLifeLink() > 0) {
            findCupidPartner(player).ifPresent(partner -> {
                loveLinks.add(partner.getIpData());
//                eliminateDuringDay(partner, eliminatedPlayers, story, false);
            });
        }
    }

    private void eliminatePlayer(DataMember player) {
        player.setDead(true);
        player.setId(player.getIdDefault());
        deadPlayers.add(player);
    }

    private void applyDeathConsequences(String deviceId, DataMember deceased, Map<String, DataMember> playersByDevice,
                                        Consumer<String> log) {
        copyLinks.getOrDefault(deviceId, List.of()).forEach(copycatDevice -> {
            DataMember copycat = playersByDevice.get(copycatDevice);
            if (copycat != null && copycat.getId() != 1) { // Nếu đã là soi thì ko thể change
                copyRoleSkills(deceased, copycat);
                log.accept("Nhân bản đã trở thành " + deceased.getRoleDefault());
            }
        });
        if (deceased.getId() == 32) {
            findPlayerByRole(37).ifPresent(apprentice -> {
                apprentice.setRole(apprentice.getRole() + " trở thành " + deceased.getRoleDefault());
                apprentice.setDisabledSkill(false);
                apprentice.setId(32);
                apprentice.setOrderCall(deceased.getOrderCall());
                log.accept("Tiên Tri Tập Sự trở thành " + deceased.getRoleDefault());
            });
        }
    }

    private void copyRoleSkills(DataMember source, DataMember target) {
        target.setRole(target.getRole() + " trở thành " + source.getRoleDefault());
        target.setId(source.getIdDefault());
        target.setKillSkill(source.getKillSkill());
        target.setProtectedSkill(source.getProtectedSkill());
        target.setSuperProtectedSkill(source.isSuperProtectedSkill());
        target.setConnectSkill(source.getConnectSkill());
        target.setDisabledSkill(source.isDisabledSkill());
        target.setOrderCall(source.getOrderCall());
    }

    public Map<String, Object> getGameManagementData() {
        LinkedHashMap<String, Map<String, Object>> actionColumns = new LinkedHashMap<>();
        List<String> superProtectedDevices = new ArrayList<>();
        DataMember wolf = new DataMember();
        for (DataMember player : players) {
            if (player.isSuperProtectedSkill()) {
                superProtectedDevices.add(player.getIpData());
            }
            if (isWolfAction(player)) {
                if (player.isDead()) {
                    wolf = player;
                    continue;
                }
                actionColumns.putIfAbsent("soi", wolfColumn(player));
            } else if (hasAction(player)) {
                actionColumns.putIfAbsent(player.getId() + "_" + player.getIpData(), roleColumn(player));
            }
        }
        actionColumns.putIfAbsent("soi", wolfColumn(wolf));// Nếu toàn bộ sói chết, add đại 1 con sói
        // Post-process: for same roleId group, keep alive ones; if all dead keep only one representative
        Map<Integer, List<String>> keysByRoleId = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : actionColumns.entrySet()) {
            if (!"soi".equals(entry.getKey())) {
                int roleId = (Integer) entry.getValue().get("roleId");
                keysByRoleId.computeIfAbsent(roleId, k -> new ArrayList<>()).add(entry.getKey());
            }
        }
        for (List<String> keys : keysByRoleId.values()) {
            if (keys.size() <= 1) continue;
            boolean anyAlive = keys.stream().anyMatch(k -> !(Boolean) actionColumns.get(k).get("isDead"));
            if (anyAlive) {
                keys.stream().filter(k -> (Boolean) actionColumns.get(k).get("isDead")).forEach(actionColumns::remove);
            } else {
                keys.stream().skip(1).forEach(actionColumns::remove);
            }
        }
        List<Map<String, Object>> columns = actionColumns.values().stream()
                .sorted(Comparator.comparing(column -> (Integer) column.get("orderCall")))
                .toList();
        Map<String, Object> data = new HashMap<>();
        data.put("players", managementPlayers());
        data.put("columns", columns);
        data.put("hasSuperProtectedPlayers", !superProtectedDevices.isEmpty());
        data.put("superProtectedDevices", superProtectedDevices);
        return data;
    }

    private Map<String, Object> wolfColumn(DataMember player) {
        return actionColumn(1, "🐺 Sói", true, false, false, false, 0, player);
    }

    private Map<String, Object> roleColumn(DataMember player) {
        boolean hasKill = player.getKillSkill() > 0;
        boolean hasProtect = player.getProtectedSkill() > 0;
        boolean hasConnect = player.getConnectSkill() > 0 && !List.of(5, 6).contains(player.getConnectSkill());
        return actionColumn(player.getId(), removeHtml(player.getRole()), false, hasKill, hasProtect, hasConnect,
                player.getConnectSkill(), player);
    }

    private Map<String, Object> actionColumn(int roleId, String label, boolean wolf, boolean hasKill, boolean hasProtect,
                                             boolean hasConnect, int connectValue, DataMember player) {
        Map<String, Object> column = new HashMap<>();
        column.put("roleId", roleId);
        column.put("label", label);
        column.put("isSoi", wolf);
        column.put("hasKill", hasKill);
        column.put("hasProt", hasProtect);
        column.put("hasConn", hasConnect);
        column.put("connValue", connectValue);
        column.put("disabled", player.isDisabledSkill());
        column.put("subCols", wolf ? 1 : (hasKill ? 1 : 0) + (hasProtect ? 1 : 0) + (hasConnect ? 1 : 0));
        column.put("deviceId", player.getIpData());
        column.put("orderCall", player.getOrderCall());
        column.put("oldTarget", player.getOldTargetId());
        column.put("isDead" , player.isDead());
        column.put("idDefault", player.getIdDefault());
        column.put("roleDefault", player.getRoleDefault());
        return column;
    }

    private List<Map<String, Object>> managementPlayers() {
        Comparator<DataMember> order = Comparator
                .comparingInt((DataMember p) -> deviceIdOrder.getOrDefault(p.getIpData(), Integer.MAX_VALUE))
                .thenComparingInt(DataMember::getId);
        return players.stream()
                .filter(player -> !player.isDead() && !ObjectUtils.isEmpty(player.getIpData()))
                .sorted(order)
                .map(player -> Map.<String, Object>of(
                        "deviceId", player.getIpData(),
                        "idPlayGame", player.getIdPlayGame(),
                        "name", player.getNameMember() != null ? player.getNameMember() : "Ẩn danh",
                        "roleName", removeHtml(player.getRole()),
                        "id", player.getId(),
                        "hasSuperProtected", player.isSuperProtectedSkill()
                ))
                .toList();
    }

    public void updateDeviceOrder(List<String> orderedDeviceIds) {
        deviceIdOrder.clear();
        for (int i = 0; i < orderedDeviceIds.size(); i++) {
            deviceIdOrder.put(orderedDeviceIds.get(i), i);
        }
    }

    private Map<String, DataMember> playersByDevice() {
        return players.stream()
                .filter(player -> !ObjectUtils.isEmpty(player.getIpData()))
                .collect(Collectors.toMap(DataMember::getIpData, player -> player, (first, ignored) -> first, LinkedHashMap::new));
    }

    private List<DataMember> livingWolves() {
        return players.stream().filter(player -> isWolf(player) && player.getId() != 15 && !player.isDead()).toList();
    }

    private String alphaWolfDevice() {
        List<DataMember> wolves = livingWolves();
        if (wolves.size() == 1) {
            return wolves.get(0).getIpData();
        }
        return wolves.stream()
                .filter(player -> player.getId() == 2 || player.getId() == 4)
                .map(DataMember::getIpData)
                .findFirst()
                .orElse(null);
    }

    private Optional<DataMember> findPlayerByRole(int roleId) {
        return players.stream().filter(player -> player.getId() == roleId).findFirst();
    }

    private Optional<DataMember> findCupidPartner(DataMember player) {
        if (player.getLifeLinkIds() == null) {
            return Optional.empty();
        }
        return players.stream().filter(candidate -> player.getLifeLinkIds().contains(candidate.getId()) && !candidate.isDead()).findFirst();
    }

    private boolean isWolf(DataMember player) {
        return WOLF_ROLE_IDS.contains(player.getId());
    }

    private boolean isWolfAction(DataMember player) {
        return isWolf(player) && player.getId() != 15;
    }

    private boolean hasAction(DataMember player) {
        return player.getKillSkill() >= 0 || player.getProtectedSkill() >= 0
                || (player.getConnectSkill() > 0 && !List.of(5, 6).contains(player.getConnectSkill()));
    }

    private void resetNightSkillBlocks() {
        players.stream()
                .filter(player -> player.getId() != 37) // TODO chi bat soi va sat thu bi disable đêm trước
                .forEach(player -> player.setDisabledSkill(false));
    }

    private void addReason(Map<String, String> reasons, String deviceId, String reason) {
        reasons.merge(deviceId, reason, (current, next) -> current.contains(next) ? current : current + " và " + next);
    }

    private void appendStory(StringBuilder story, String message) {
        story.append(" - ").append(message).append("<br>");
    }

    private DataMember roleSummary(DataMember role, int count) {
        return DataMember.builder()
                .id(role.getId())
                .role(role.getRole())
                .description(role.getDescription())
                .total(String.valueOf(count))
                .build();
    }

    private String removeHtml(String value) {
        return value.replaceAll("<[^>]*>", "");
    }

    public void createTest(int total) {
        String[] firstNames = {"An", "Bình", "Chi", "Dũng", "Hà", "Hải", "Hùng", "Lan", "Linh", "Mai",
                "Minh", "Nam", "Ngọc", "Phong", "Quân", "Sơn", "Thảo", "Trang", "Tuấn", "Việt"};
        for (int index = 0; index < Math.min(total, players.size()); index++) {
            DataMember player = players.get(index);
            player.setIpData(UUID.randomUUID().toString());
            player.setNameMember(firstNames[index % firstNames.length] + " " + (index + 1));
        }
    }

    private static final class NightResolution {
        private final Map<String, String> disabledRoles = new LinkedHashMap<>();
        private final Map<String, String> nextDayBlocks = new LinkedHashMap<>();
        private final Map<String, String> deaths = new LinkedHashMap<>();
        private final Map<String, String> unpreventableDeaths = new LinkedHashMap<>();
        private final Map<String, String> savedPlayers = new LinkedHashMap<>();
        private final Map<String, String> hunterLinks = new LinkedHashMap<>();
        private final List<String> events = new ArrayList<>();
        private boolean superProtectionActive;
        private boolean sickWolfWasTargeted; // hiện thị đã cạp trúng sói si đa
    }
}
