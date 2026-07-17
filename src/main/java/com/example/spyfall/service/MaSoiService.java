package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.SoiNguyenDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class MaSoiService {

    private List<DataMember> datas = new ArrayList<>();
    private List<DataMember> pls = new ArrayList<>();
    private List<DataMember> listShowForMember = new ArrayList<>();
    private boolean checkDecreaseSoi = false;
    private boolean isGameEnd = true;
    private boolean isAddSoiNguyen = false;
    private boolean isHaveSoiNguyen = false;
    private Map<Integer, List<DataMember>> detailEachGame = new TreeMap<>(Comparator.reverseOrder());
    private final String image = "/qrcode.png";

    public final List<String> ID_SOI = List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15");
    public final List<String> ID_OUTSIDER = List.of("21", "20", "22");

    public String getImage() { return image; }
    public List<DataMember> getDatas() { dataInputService.prepareDataMaSoi(datas); return datas; }
    public List<DataMember> getPls() { return pls; }
    public List<DataMember> getListShowForMember() { return listShowForMember; }
    public int getGameNumber() { return detailEachGame.size() + 1; }

    @Autowired
    DataInputService dataInputService;

    public boolean isShowSoiNguyen() {
        return isHaveSoiNguyen && !isAddSoiNguyen;
    }

    public String loadGame(Map<String, String> params) {
        if (!isGameEnd) return "GAME NOT END YES";
        int totalPlay = Integer.parseInt(params.get("total"));
        if (totalPlay == 0) return "ERROR LỖI KHÔNG TỔNG NGƯỜI";

        boolean isSoiNguyen = Boolean.parseBoolean(params.get("checkSoi"));
        boolean isCupid = Boolean.parseBoolean(params.get("checkCupid"));

        isGameEnd = false;
        dataInputService.prepareDataMaSoi(datas);
        pls.clear();
        listShowForMember.clear();
        checkDecreaseSoi = false;
        isHaveSoiNguyen = false;

        int totalRole = 0;
        for (DataMember data : datas) {
            int total = 0;
            if (params.get(data.getId()) != null) {
                total = Integer.parseInt(params.get(data.getId()));
            }
            if (total >= 1) {
                listShowForMember.add(createDM(data, total));
            }
            for (int i = 0; i < total; i++) {
                data.setIpData("play");
                pls.add(DataMember.builder()
                        .id(data.getId()).idPlayGame(String.valueOf(pls.size() + 1))
                        .location(data.getLocation()).description(data.getDescription()).build());
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
            if (StringUtils.pathEquals(item.getId(), "3")) isHaveSoiNguyen = true;
        });

        if (isSoiNguyen && checkDecreaseSoi) {
            for (DataMember item : pls) {
                if (ID_SOI.contains(item.getId())) {
                    item.setLocation(item.getLocation() + " + Sói Nguyền");
                    item.setDescription(item.getDescription() + "sói nguyền");
                    isHaveSoiNguyen = true;
                }
            }
        }

        if (isCupid) {
            Collections.shuffle(pls);
            List<DataMember> cupid = pls.subList(0, 2);
            String c1 = cupid.get(0).getLocation(), c2 = cupid.get(1).getLocation();
            cupid.get(0).setLocation(c1 + "<i id=\"cupid\"> cặp đôi với " + c2 + "</i>");
            cupid.get(1).setLocation(c2 + "<i id=\"cupid\"> cặp đôi với " + c1 + "</i>");
        }
        return "OK load";
    }

    /**
     * Get or assign role for the player. Returns DataMember assigned or null if not set up.
     */
    public DataMember getOrAssignRole(String clientIp, String name) {
        if (pls.isEmpty()) return null;
        else {
            for (DataMember dataMember : pls) {
                if (clientIp.equals(dataMember.getIpData())) {
                    if (name != null) dataMember.setNameMember(name);
                    return dataMember;
                }
            }
        }

        Collections.shuffle(pls);
        pls.sort(Comparator.comparing((DataMember m) -> m.getIpData(), Comparator.nullsFirst(String::compareTo)));
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
            value.sort(Comparator.comparing(m -> Integer.parseInt(m.getId())));
            value.forEach(item -> {
                Map<String, Object> player = new HashMap<>();
                String displayLocation = item.getLocation().replaceAll("<[^>]*>", "");
                String name = item.getNameMember() != null ? item.getNameMember() : (item.getIpData() != null ? "Ẩn danh" : "Chưa nhận");
                
                player.put("id", item.getId());
                player.put("displayName", name);
                player.put("displayLocation", displayLocation);
                player.put("isSoi", ID_SOI.contains(item.getId()));
                player.put("isOutsider", ID_OUTSIDER.contains(item.getId()));
                players.add(player);
            });
            
            game.put("players", players);
            games.add(game);
        });
        
        Map<String, Object> result = new HashMap<>();
        result.put("games", games);
        return result;
    }

    public String endGame() {
        detailEachGame.put(detailEachGame.size() + 1, new ArrayList<>(pls));
        isGameEnd = true;
        isAddSoiNguyen = false;
        pls.clear();
        listShowForMember.clear();
        checkDecreaseSoi = false;
        return "End Game : " + detailEachGame.size();
    }

    public String soiNguyen(SoiNguyenDto dto) {
        if (isAddSoiNguyen) return "Soi Nguyen Da Add TRUOc DO";
        isAddSoiNguyen = true;
        AtomicReference<String> name = new AtomicReference<>("");
        pls.forEach(item -> {
            if (StringUtils.pathEquals(item.getIdPlayGame(), dto.getSoiNguyen())) {
                item.setId("1");
                item.setLocation(item.getLocation() + " - Soi");
                name.set(item.getLocation());
            }
        });
        return "add Soi Nguyen: " + name;
    }

    public String getActiveRolesString() {
        StringBuilder sb = new StringBuilder();
        for (DataMember item : pls) {
            if (!item.getId().equals("1") && !item.getId().equals("30")) {
                sb.append(item.getLocation().replaceAll("<[^>]*>", "")).append(", ");
            }
        }
        String result = sb.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    private DataMember createDM(DataMember d, Integer total) {
        return DataMember.builder().id(d.getId()).location(d.getLocation()).description(d.getDescription()).total(String.valueOf(total)).build();
    }
}

