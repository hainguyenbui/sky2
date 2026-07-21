package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.LifeLinkDto;
import com.example.spyfall.common.NightActionDto;
import com.example.spyfall.common.SoiNguyenDto;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class MaSoiService {

    private List<DataMember> datas = new ArrayList<>();
    private List<DataMember> pls = new ArrayList<>();
    private List<DataMember> deadPls = new ArrayList<>();
    private List<DataMember> listShowForMember = new ArrayList<>();
    private boolean checkDecreaseSoi = false;
    private boolean isGameEnd = true;
    private boolean isAddSoiNguyen = false;
    private boolean isHaveSoiNguyen = false;
    @Getter
    private boolean allowDeadViewGameHistory = false;
    @Getter
    private boolean allowShowAliveDead = false;
    private Map<Integer, List<DataMember>> detailEachGame = new TreeMap<>(Comparator.reverseOrder());
    @Getter
    private Map<String, String> detailOneGameHistory = new LinkedHashMap<>();
    public String detailGameDay = "";
    @Getter
    private Map<Integer, Map<String, String>> detailAllGame = new LinkedHashMap<>();
    public boolean dayIsReadyKill = false;
    private final String image = "/qrcode.png";
    private Map<String, List<String>> linkRole = new HashMap<>();// Liên kết sinh mệnh chức năng nếu key chết value sẽ có chức năng;
    private List<String> historyAdmin = new ArrayList<>();
    private List<String> howToDie = List.of(" bị thủ tiêu vì biết quá nhiều", " không muốn chơi nữa", " bị thù ghét", " nói quá nhiều");
    public int countNight = 1;

    public final List<Integer> ID_SOI = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);
    public final List<Integer> ID_OUTSIDER = List.of(21, 20, 22);

    public String getImage() { return image; }
    public List<DataMember> getDatas() throws Exception { dataInputService.prepareDataMaSoi(datas); return datas; }
    public List<DataMember> getPls() { return pls; }
    public List<DataMember> getDeadPls() { return deadPls; }
    public List<DataMember> getListShowForMember() { return listShowForMember; }
    public int getGameNumber() { return detailEachGame.size() + 1; }
    public void setAllowDeadViewGameHistory(boolean value) { allowDeadViewGameHistory = value; }
    public void setAllowShowAliveDead(boolean value) { allowShowAliveDead = value; }


    @Autowired
    DataInputService dataInputService;

    public boolean isShowSoiNguyen() {
        return isHaveSoiNguyen && !isAddSoiNguyen;
    }

    public String loadGame(Map<String, String> params) throws Exception{
        if (!isGameEnd) return "ALERT GAME NOT END YES";
        int totalPlay = Integer.parseInt(ObjectUtils.isEmpty(params.get("total")) ? "0" : params.get("total"));
        if (totalPlay == 0) return "ERROR LỖI KHÔNG TỔNG NGƯỜI";

        boolean isSoiNguyen = Boolean.parseBoolean(params.get("checkSoi"));
        boolean isCupid = Boolean.parseBoolean(params.get("checkCupid"));

        isGameEnd = false;
        dataInputService.prepareDataMaSoi(datas);
        pls.clear();
        deadPls.clear();
        listShowForMember.clear();
        checkDecreaseSoi = false;
        isHaveSoiNguyen = false;
        allowDeadViewGameHistory = false;
        allowShowAliveDead = false;
        countNight = 1;

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
                        .build());
            }
            totalRole += total;
        }

        Random random = new Random();
        for (int i = 0; i < totalRole - totalPlay; i++) {
            int indexMember = random.nextInt(pls.size());
            if (ID_SOI.contains(pls.get(indexMember).getId())) checkDecreaseSoi = true;
            pls.remove(indexMember);
        }

        pls.forEach(item -> {
            if (Objects.equals(item.getId(), 3)) isHaveSoiNguyen = true;
        });

        if (isSoiNguyen || checkDecreaseSoi) {
            for (DataMember item : pls) {
                if (ID_SOI.contains(item.getId())) {
                    item.setRole(item.getRole() + " + Sói Nguyền");
                    item.setDescription(item.getDescription() + "sói nguyền");
                    isHaveSoiNguyen = true;
                }
            }
        }

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
    public DataMember getOrAssignRole(String clientIp, String name) {
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
        for (int i = 0; i < pls.size() - 1; i++) {
            DataMember data = pls.get(i);
            data.setIpData("dfafsfdsfdsf" + (i+1));
            data.setNameMember("h" + (i +1) );
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

    public Map<String, Object> getGameHistoryData() {
        List<Map<String, Object>> games = new ArrayList<>();
        detailEachGame.forEach((key, value) -> {
            Map<String, Object> game = new HashMap<>();
            game.put("gameNumber", key);
            
            List<Map<String, Object>> players = new ArrayList<>();
            value.sort(Comparator.comparing(DataMember::getId));
            value.forEach(item -> {
                Map<String, Object> player = new HashMap<>();
                String displayLocation = item.getRole().replaceAll("<[^>]*>", "");
                String name = item.getNameMember() != null ? item.getNameMember() : (item.getIpData() != null ? "Ẩn danh" : "Chưa nhận");
                
                player.put("id", item.getId());
                player.put("displayName", name);
                player.put("displayLocation", displayLocation);
                player.put("isSoi", ID_SOI.contains(item.getId()));
                player.put("isOutsider", ID_OUTSIDER.contains(item.getId()));
                players.add(player);
            });
            game.put("details", detailAllGame.get(key));
            game.put("players", players);
            games.add(game);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("games", games);
        return result;
    }

    public String endGame() {
        detailEachGame.put(detailEachGame.size() + 1, new ArrayList<>(pls));
        detailAllGame.put(detailEachGame.size(), new LinkedHashMap<>(detailOneGameHistory));
        isGameEnd = true;
        isAddSoiNguyen = false;
        pls.clear();
        listShowForMember.clear();
        checkDecreaseSoi = false;
        countNight = 1;
        detailOneGameHistory.clear();
        dayIsReadyKill = false;
        return "End Game : " + detailEachGame.size();
    }

    public String getActiveRolesString() {
        Set<String> roleAll = new LinkedHashSet<>();
        pls.sort(Comparator.comparing(DataMember::getId));
        List<Integer> notShow = List.of(21,30, 35, 41);
        for (DataMember item : pls) {
            if (item.getId() < 15) {
                roleAll.add("Soi");
            } else if (!notShow.contains(item.getId())) {
                roleAll.add(item.getRole());
            }
        }
        return String.join(", ", roleAll);
    }

    public String processNight(List<NightActionDto> actions) {
        dayIsReadyKill = false;
        // BE nhận toàn bộ hành động đêm, tự xử lý logic ai chết ai không
        // FE đã gửi: deviceId (mục tiêu), roleId, roleName, colType (soi/kill/prot/conn), connValue
        Map<String, String> disabledRole = new LinkedHashMap<>(); // kiem tra role nao mat phep
        Map<String, String> disabledRoleNextDay = new LinkedHashMap<>(); // kiem tra role nao ngay hom sau mat phep
        Map<String, String> toKill  = new LinkedHashMap<>();
        Map<String, String> toSupperKill  = new LinkedHashMap<>();
        Map<String, String> toSave  = new LinkedHashMap<>();
        Map<String, LifeLinkDto> toLifeLink  = new LinkedHashMap<>();
//        Map<Integer, DataMember> playerByRoles = pls.stream().collect(Collectors.toMap(DataMember::getId, player -> player));
        Map<String, DataMember> playerByIpData = pls.stream().collect(Collectors.toMap(DataMember::getIpData, player -> player));
        boolean isSuperProtectedSkill = false;
        List<String> addHistory = new ArrayList<>();
        pls.forEach(player -> {
            if (player.getId() != 41 && player.getId() != 5) {
                player.setDisabledSkill(false);
            }
        });
        historyAdmin.clear();
        // loc lay soi con song la soi dau dan hoac la con soi duy nhat
        String soiDauDanId = checkAlphaWolf();
        List<String> livingWolves = pls.stream()
                .filter(player -> ID_SOI.contains(player.getId()) && player.getId() != 15)
                .map(DataMember::getIpData)
                .toList();

        // kiem tra cac chuc nang cam lang truoc
        for (NightActionDto a : actions) {
            if (a.getRoleId() == 38) {
                // neu là câm lặng thì check người bị câm lặng có phải là sói còn sống hay không
                if (livingWolves.contains(a.getTargetDeviceId())) {
                    if (Objects.equals(a.getTargetDeviceId(), soiDauDanId)) {
                        if (Objects.equals(a.getTargetRoleId(), 5)) {
                            disabledRole.put(a.getTargetDeviceId(), a.getTargetName() + ": " + a.getTargetRoleName() + " bị câm lặng");
                            continue;
                        }
                        disabledRole.put("soi", a.getTargetName() + ": " + a.getTargetRoleName() + " bị câm lặng");
                    }
                } else {
                    disabledRole.put(a.getTargetDeviceId(), a.getTargetName() + ": " + a.getTargetRoleName() + " bị câm lặng");
                }
            } else if (a.getRoleId() == 42) {
                // nếu là nhân bản thì liên kết sinh mệnh
                DataMember dataMember = playerByIpData.get(a.getDeviceId());
                dataMember.setDisabledSkill(true);
                linkRole.put(a.getTargetDeviceId(), List.of(dataMember.getIpData()));
                addHistory.add("Nhân bản đã chọn " + a.getTargetRoleName());
            } else if (a.getRoleId() == 44 && Objects.equals(a.getConnValue(), "ON")) {
                isSuperProtectedSkill = true;
                addHistory.add("Thiên thần đã bảo vệ lượt này");
                pls.stream().filter(player -> player.getId() == 44).findFirst().ifPresent(player -> player.setSuperProtectedSkill(false));
            } else if (playerByIpData.get(a.getTargetDeviceId()) != null && Objects.equals(playerByIpData.get(a.getTargetDeviceId()).getId(), 41)) {
                // nếu là người bệnh bị cắn thì disable chức năng của sói và sát thủ vào hôm sau
                if (isSuperProtectedSkill) continue;
                if (a.getRoleId() == 1) {
                    pls.forEach(player -> {
                        if (ID_SOI.contains(player.getId()) && player.getId() != 15) {
                            player.setDisabledSkill(true);
                        }
                    });
                    disabledRoleNextDay.put("Soi", "Sói cạp trúng người bệnh");
                } else if (a.getRoleId() == 20) {
                    playerByIpData.get(a.getDeviceId()).setDisabledSkill(true);
                    disabledRoleNextDay.put("Sát thủ", "Sát thủ cạp trúng người bệnh");
                }
            }
        }

        DataMember soiSida = pls.stream().filter(player -> player.getId() == 5).findFirst().orElse(null);
        boolean isHaveSoiSida = false;
        for (NightActionDto a : actions) {
            if (soiSida != null && !soiSida.isDisabledSkill() && Objects.equals(soiSida.getIpData(), a.getTargetDeviceId())) {
                if (!disabledRole.containsKey(a.getTargetDeviceId())) {
                    toKill.put(a.getDeviceId(), "đụng phải Sói bị Sida");
                }
                isHaveSoiSida = true;
            }
            switch (a.getColType()) {
                case "soi":
                    if (disabledRole.containsKey("soi")) break;
                    DataMember data = playerByIpData.get(a.getTargetDeviceId());
                    if (data != null && data.getId() == 40) {
                        data.setId(1);
                        data.setRole(data.getRole() + " + bạn đã là Sói");
                        addHistory.add(a.getTargetName() + ": " + a.getTargetRoleName() + " đã trở thành Sói");
                        break;
                    } else if (data != null && data.getId() == 20) {
                        addHistory.add("Sói cắn hụt sát thủ");
                        break;
                    }
                    toKill.merge(a.getTargetDeviceId(), "bị Soi Cắn", (oldValue, newValue) -> oldValue + " và " + newValue);
                    break;
                case "kill":
                    playerByIpData.get(a.getDeviceId()).decreaseKillSkill();
                    if (disabledRole.containsKey(a.getDeviceId())) break;
                    if (a.getRoleId() == 31) {
                        // dame phù thủy không thể cứu
                        toSupperKill.put(a.getTargetDeviceId(), "bị " + a.getRoleName() + " quăng bình");
                        break;
                    } else if (a.getRoleId() == 43) {
                        // neu la độc tài thì kiểm tra giết phải dân ko
                        if (a.getTargetRoleId() >= 30) {
                            toKill.put(a.getDeviceId(), " loại bỏ nhầm dân");
                        }
                    } else if (a.getTargetRoleId() < 30 && a.getRoleId() == 46) {
                        addHistory.add("Boom giết phe không phải dân");
                        DataMember dataMember = playerByIpData.get(a.getDeviceId());
                        dataMember.setKillSkill(1);
                    }

                    toKill.merge(a.getTargetDeviceId(), "bị " + a.getRoleName() + " tác động vật lý", (oldValue, newValue) -> oldValue + " và " + newValue);
                    break;
                case "prot":
                    playerByIpData.get(a.getDeviceId()).decreaseProtectedSkill();
                    if (disabledRole.containsKey(a.getDeviceId())) break;
                    toSave.merge(a.getTargetDeviceId(), "được " + a.getRoleName() + " bảo vệ", (oldValue, newValue) -> oldValue + " và " + newValue);
                    break;
                case "conn":
                    if (disabledRole.containsKey(a.getDeviceId())) break;
                    if (Objects.equals(a.getRoleId(), 34)) {
                        // neu la tho san thi lien ket sinh mang
                        toLifeLink.put(a.getDeviceId(), new LifeLinkDto(a.getTargetDeviceId(), "bị thợ săn ghim"));
                    }
                    if (Objects.equals(a.getRoleId(), 45)) {
                        historyAdmin.add("Phù Thủy Già đuổi " + a.getTargetName() + " ra khòi làng");
                    }
                    break;
                case "recruit":
                    DataMember targetMember = playerByIpData.get(a.getTargetDeviceId());
                    targetMember.setId(1);
                    targetMember.setRole(targetMember.getRole() + " - Soi");
                    historyAdmin.add(a.getTargetName() + " đã trở thành Sói");
                    addHistory.add(a.getTargetName() + " đã trở thành Sói");
                    isHaveSoiNguyen = false;
            }
        }
        if (isHaveSoiSida) {
            soiSida.setDisabledSkill(true);
        }

        toSave.forEach((key, value) -> toKill.remove(key));
        for (Map.Entry<String, String> entry : toSupperKill.entrySet()) {
            if (toKill.containsKey(entry.getKey()) && toKill.get(entry.getKey()).contains(entry.getValue())) {
                continue; // tránh lặp lại nếu đã có lý do chết
            }
            toKill.merge(entry.getKey(), entry.getValue(), (oldValue, newValue) -> oldValue + " và " + newValue);

        }

        int sizeKill;
        do {
            sizeKill = toKill.size();
            // check co chết vì liên kết sinh mệnh thợ săn
            for (Map.Entry<String, LifeLinkDto> entry : toLifeLink.entrySet()) {
                if (toKill.containsKey(entry.getKey())) {
                    if (toKill.containsKey(entry.getValue().getDeviceId()) && toKill.get(entry.getValue().getDeviceId()).contains(entry.getValue().getResonKill())) {
                        continue; // tránh lặp lại nếu đã có lý do chết
                    }
                    toKill.merge(entry.getValue().getDeviceId(), entry.getValue().getResonKill() , (oldValue, newValue) -> oldValue + " và " + newValue);
                }
            }
            // check co chết vì là cặp đôi
            var listKeySet = new ArrayList<>(toKill.keySet());
            for (String ipData : listKeySet) {
                DataMember player = playerByIpData.get(ipData);
                if (player != null && player.getLifeLink() > 0) {
                    for (Integer linkedId : player.getLifeLinkIds()) {
                        DataMember target = pls.stream().filter(playerLink -> playerLink.getId() == linkedId).findFirst().orElse(new DataMember());
                        if (!ObjectUtils.isEmpty(target)) {
                            toKill.merge(target.getIpData(), "", (oldValue, newValue) -> oldValue);
                        }
                    }
                }
            }
        } while (sizeKill != toKill.size());
        if (isSuperProtectedSkill) {
            toKill.clear();// Thiên thần bảo vệ không ai phải chết
        }
        return kill(toKill, playerByIpData, toSave, disabledRole, disabledRoleNextDay, addHistory);
    }

    public String kill(List<String> deviceIds) {
        dayIsReadyKill = true;
        StringBuilder detailDay = new StringBuilder();
        Map<String, DataMember> tokill = new LinkedHashMap<>();
        deviceIds.forEach(deviceId -> {
            DataMember player = pls.stream().filter(item -> Objects.equals(item.getIpData(), deviceId)).findFirst().orElse(null);
            if (player != null) {
                Random random = new Random();
                int number = random.nextInt(howToDie.size());

                player.setDead(true);
                deadPls.add(player);
                detailDay.append(player.getNameMember()).append(": ").append(player.getRole()).append(howToDie.get(number)).append("<br>");
                tokill.put(deviceId, player);
                if (player.getLifeLink() > 0) {
                    for (Integer linkedId : player.getLifeLinkIds()) {
                        DataMember target = pls.stream().filter(playerLink -> playerLink.getId() == linkedId).findFirst().orElse(new DataMember());
                        if (!ObjectUtils.isEmpty(target)) {
                            target.setDead(true);
                            deadPls.add(target);
                            detailDay.append(target.getNameMember()).append(": ").append(target.getRole()).append("<br>");
                            tokill.put(target.getIpData(), target);
                        }
                    }
                }
            }
        });
        tokill.forEach((k, dieMember) -> {
            if (linkRole.containsKey(k)) {
                List<String> linkedDevices = linkRole.get(k);
                for (String linkedDevice : linkedDevices) {
                    DataMember linkedPlayer = pls.stream().filter(player -> Objects.equals(player.getIpData(), linkedDevice)).findFirst().orElse(null);
                    if (!ObjectUtils.isEmpty(linkedPlayer)) {
                        linkedPlayer.setRole(linkedPlayer.getRole() + " trở thành " + dieMember.getRole());// chức năng nhân bản
                        linkedPlayer.setId(dieMember.getId());
                        linkedPlayer.setKillSkill(dieMember.getKillSkill());
                        linkedPlayer.setProtectedSkill(dieMember.getProtectedSkill());
                        linkedPlayer.setSuperProtectedSkill(dieMember.isSuperProtectedSkill());
                        linkedPlayer.setConnectSkill(dieMember.getConnectSkill());
                        linkedPlayer.setDisabledSkill(dieMember.isDisabledSkill());
                        detailDay.append("Nhân bản đã trở thành ").append(dieMember.getRole()).append("<br>");

                    }
                }
            }

            if (dieMember.getId() == 32) {
                // nếu là tiên tri thì check xem có tiên tri tập sự hay ko
                pls.stream().filter(player -> player.getId() == 37).findFirst().ifPresent(tienTriTapSu -> tienTriTapSu.setRole(tienTriTapSu.getRole() + " trở thành " + dieMember.getRole()));
                detailDay.append("Tiên Tri Tập Sư trở thành ").append(dieMember.getRole()).append("<br>");
            }
        });
        if (!detailDay.isEmpty()) {
            detailGameDay = detailDay.toString();
            try {
                detailOneGameHistory.put("Ngày " + countNight, detailDay.toString());
            } catch (Exception ignored){}
        }
        return "Detail: " + detailDay;
    }

    /**
     * Lấy ra sói đầu đàn hoặc là con sói duy nhất
     * @return id sói
     */
    private String checkAlphaWolf() {
        String alphaWolfId = pls.stream()
                .filter(player -> player.getId() == 2 || player.getId() == 4)
                .map(DataMember::getIpData)
                .findFirst()
                .orElse(null);
        if (alphaWolfId != null) {
            // Lấy danh sách sói còn sống
            List<DataMember> livingWolves = pls.stream()
                    .filter(player -> ID_SOI.contains(player.getId()) && player.getId() != 15)
                    .toList();
            if (livingWolves.size() == 1) {
                // Nếu chỉ còn một sói, đánh dấu là sói đầu đàn
                alphaWolfId = livingWolves.get(0).getIpData();
            }
        }
        return alphaWolfId;
    }

    public String kill(Map<String, String> toKill, Map<String, DataMember> playerByIpData, Map<String, String> toSave,
                       Map<String, String> disabledRole, Map<String, String> disabledRoleNextDay, List<String> addHistory) {
        StringBuilder nightStory = new StringBuilder();

        addHistory.forEach(s -> nightStory.append(" - ").append(s).append("<br>"));
        disabledRole.forEach((k, v) -> {
            nightStory.append(" - ").append(v).append("<br>");
        });
        toSave.forEach((k, v) -> {
            DataMember targetMember = playerByIpData.get(k);
            nightStory.append(" - ").append(targetMember.getNameMember()).append(": ").append(targetMember.getRole()).append(" ").append(v).append(" <br>");
        });
        toKill.forEach((k, v) -> {
            DataMember targetMember = playerByIpData.get(k);
            nightStory.append(" - ").append(targetMember.getNameMember()).append(": ").append(targetMember.getRole()).append(" ").append(v).append(" <br>");
//            pls.remove(playerByIpData.get(k));
            targetMember.setDead(true);
            deadPls.add(playerByIpData.get(k));
            historyAdmin.add(playerByIpData.get(k).getNameMember() + " bị loại");
            DataMember dieMember = playerByIpData.get(k);
            if (linkRole.containsKey(k)) {
                List<String> linkedDevices = linkRole.get(k);
                for (String linkedDevice : linkedDevices) {
                    DataMember linkedPlayer = playerByIpData.get(linkedDevice);
                    if (!ObjectUtils.isEmpty(linkedPlayer) && !ObjectUtils.isEmpty(dieMember)) {
                        linkedPlayer.setRole(linkedPlayer.getRole() + " trở thành " + dieMember.getRole());// chức năng nhân bản
                        linkedPlayer.setId(dieMember.getId());
                        linkedPlayer.setKillSkill(dieMember.getKillSkill());
                        linkedPlayer.setProtectedSkill(dieMember.getProtectedSkill());
                        linkedPlayer.setSuperProtectedSkill(dieMember.isSuperProtectedSkill());
                        linkedPlayer.setConnectSkill(dieMember.getConnectSkill());
                        linkedPlayer.setDisabledSkill(dieMember.isDisabledSkill());
                        addHistory.add("Nhân bản đã trở thành " + dieMember.getRole());
                        historyAdmin.add("Nhân bản đã trở thành " + dieMember.getRole());
                    }
                }
            }

            if (dieMember.getId() == 32) {
                // nếu là tiên tri thì check xem có tiên tri tập sự hay ko
                pls.stream().filter(player -> player.getId() == 37).findFirst().ifPresent(tienTriTapSu -> tienTriTapSu.setRole(tienTriTapSu.getRole() + " trở thành " + dieMember.getRole()));
                historyAdmin.add("Tiên Tri Tập Sư trở thành " + dieMember.getRole());
                nightStory.append(" - ").append("Tiên Tri Tập Sư trở thành ").append(dieMember.getRole()).append("<br>");
            }
        });
        disabledRoleNextDay.forEach((k, v) -> {
            nightStory.append(" - ").append(v).append("<br>");
        });
        detailOneGameHistory.put("Đêm " + countNight++, nightStory.toString());
        if (toKill.isEmpty()) historyAdmin.add("Không có ai bị giết đêm nay");
        return "Detail: " + historyAdmin.stream().map(s -> System.lineSeparator() + s).collect(Collectors.joining());
    }

    public Map<String, Object> getGameManagementData() {
        Map<String, Object> data = new HashMap<>();

        // Sói: 1 cột duy nhất (key="soi") nếu còn bất kỳ role nào trong ID_SOI (trừ 15) sống
        // Các role khác: mỗi roleId duy nhất có skill → 1 cột riêng
        LinkedHashMap<String, Map<String, Object>> colMap = new LinkedHashMap<>();
        boolean hasSuperProtectedPlayers = false;
        List<String> superProtectedDevices = new ArrayList<>();

        for (DataMember player : pls) {
            if (player.isDead()) continue;
            int roleId = player.getId();
            if (player.isSuperProtectedSkill()) {
                hasSuperProtectedPlayers = true;
                superProtectedDevices.add(player.getIpData());
            }
            boolean isSoi   = ID_SOI.contains(roleId) && roleId != 15;
            boolean hasKill = player.getKillSkill() > 0;
            boolean hasProt = player.getProtectedSkill() > 0;
            boolean hasConn = player.getConnectSkill() > 0 && !List.of(6,5,4).contains( player.getConnectSkill());

            // Cột Sói: 1 cột chung cho tất cả sói
            if (isSoi && !colMap.containsKey("soi")) {
                Map<String, Object> col = new HashMap<>();
                col.put("roleId",   1);
                col.put("label",    "🐺 Sói");
                col.put("isSoi",    true);
                col.put("hasKill",  false);
                col.put("hasProt",  false);
                col.put("hasConn",  false);
                col.put("connValue",0);
                col.put("disabled", player.isDisabledSkill());
                col.put("subCols",  1);
                col.put("deviceId", player.getIpData());
                colMap.put("soi", col);
            }
            // Các role khác: mỗi roleId 1 cột nếu có skill
            if (!isSoi && (hasKill || hasProt || hasConn) && !colMap.containsKey(String.valueOf(roleId))) {
                Map<String, Object> col = new HashMap<>();
                col.put("roleId",    roleId);
                col.put("label",     player.getRole().replaceAll("<[^>]*>", ""));
                col.put("isSoi",     false);
                col.put("hasKill",   hasKill);
                col.put("hasProt",   hasProt);
                col.put("hasConn",   hasConn);
                col.put("connValue", player.getConnectSkill());
                col.put("disabled",  player.isDisabledSkill());
                int sub = (hasKill?1:0) + (hasProt?1:0) + (hasConn?1:0);
                col.put("subCols",   sub);
                col.put("deviceId", player.getIpData());
                colMap.put(String.valueOf(roleId), col);
            }
        }
        var sorted = colMap.values().stream().sorted(Comparator.comparing(m -> (Integer) m.get("roleId"))).toList();

        // Build player rows — each player carries their current ON/OFF state per skill
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
            p.put("hasSuperProtected", player.isSuperProtectedSkill());
            players.add(p);
        }
        players.sort(Comparator.comparing(m -> (Integer) m.get("id")));
        data.put("players",                  players);
        data.put("columns",                  new ArrayList<>(sorted));
        data.put("hasSuperProtectedPlayers", hasSuperProtectedPlayers);
        data.put("superProtectedDevices",    superProtectedDevices);

        return data;
    }

    private DataMember createDM(DataMember d, Integer total) {
        return DataMember.builder().id(d.getId()).role(d.getRole()).description(d.getDescription()).total(String.valueOf(total)).build();
    }
}

