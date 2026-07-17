package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.SoiNguyenDto;
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
    public List<DataMember> getDatas() { prepareData(); return datas; }
    public List<DataMember> getPls() { return pls; }
    public List<DataMember> getListShowForMember() { return listShowForMember; }
    public boolean isGameEnd() { return isGameEnd; }
    public boolean isHaveSoiNguyen() { return isHaveSoiNguyen; }
    public boolean isAddSoiNguyen() { return isAddSoiNguyen; }
    public int getGameNumber() { return detailEachGame.size() + 1; }

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
        prepareData();
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
        prepareData();
        if (!pls.isEmpty()) {
            for (DataMember dataMember : pls) {
                if (clientIp.equals(dataMember.getIpData())) {
                    if (name != null) dataMember.setNameMember(name);
                    return dataMember;
                }
            }
        }
        if (pls.isEmpty()) return null;

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

    public String buildHistoryHtml() {
        StringBuilder result = new StringBuilder();
        detailEachGame.forEach((key, value) -> {
            result.append("<div class=\"history-game-card\">\n")
                  .append("    <h4 style=\"font-size: 1rem; color: #60a5fa; margin-bottom: 10px;\">Ván ").append(key).append(":</h4>\n")
                  .append("    <div class=\"item-list\">\n");
            value.sort(Comparator.comparing(m -> Integer.parseInt(m.getId())));
            value.forEach(item -> {
                String badgeClass = ID_SOI.contains(item.getId()) ? "badge-red" : (ID_OUTSIDER.contains(item.getId()) ? "badge-orange" : "badge-green");
                String displayLocation = item.getLocation().replaceAll("<[^>]*>", "");
                String name = item.getNameMember() != null ? item.getNameMember() : (item.getIpData() != null ? "Ẩn danh" : "Chưa nhận");
                result.append("        <div class=\"item-row\" style=\"padding: 8px 12px; font-size: 0.85rem;\">\n")
                      .append("            <span style=\"font-weight: 500;\">").append(name).append("</span>\n")
                      .append("            <span class=\"badge ").append(badgeClass).append("\">").append(displayLocation).append("</span>\n")
                      .append("        </div>\n");
            });
            result.append("    </div>\n</div>\n");
        });
        return result.toString();
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

    private void prepareData() {
        if (datas.isEmpty()) {
            datas.add(createDM("1", "Sói", "Sói đó, sói nè, sói chính hiệu 9999"));
            datas.add(createDM("2", "Sói đầu đàn", "Sói có quyền quyết định cắn ai"));
            datas.add(createDM("3", "Sói nguyền", "Nguyền 1 người chơi, người đó sẽ là sói"));
            datas.add(createDM("4", "Sói đầu đàn(2)", "Sói tiên tri phải soi 2 lần"));
            datas.add(createDM("5", "Sói si đa", "1 lần duy nhất chức năng nào chạm vào bạn thì chức năng đó chết"));
            datas.add(createDM("6", "Kẻ phản bội", "Theo phe sói, biết sói là ai, sói không biết bạn"));
            datas.add(createDM("20", "Sát thủ", "Phe thứ 3, có quyền giết sói nhưng sói ko giết lại được."));
            datas.add(createDM("21", "Chán đời", "Muốn chết nhưng ko muốn bị sói cắn. Khi bị treo cổ bạn sẽ win"));
            datas.add(createDM("30", "Dân", "Dân giàu nước mạnh"));
            datas.add(createDM("31", "Phù thủy", "Có 1 bình cứu và 1 bình giết. Khi đã sài phép cứu thì sẽ ko biết ai bị giết nữa"));
            datas.add(createDM("32", "Tiên tri", "Soi 1 người có phải Sói hay ko"));
            datas.add(createDM("33", "Bảo vệ", "Mỗi đêm bảo vệ 1 người, ko bảo vệ 1 người 2 đêm liên tiếp"));
            datas.add(createDM("34", "Thợ săn", "Ghim 1 người, chỉ trong đêm nếu bạn chết người đó chết theo"));
            datas.add(createDM("35", "Thanh niên cứng", "Khi bị treo cổ có quyền lật bài và giết 1 người, bạn vẫn sống như bình thường"));
            datas.add(createDM("37", "Tiên tri tập sự", "Khi tiên tri chết thì bạn là Tiên tri"));
            datas.add(createDM("38", "Câm lặng", "Chọn 1 người và cấm phép người đó."));
            datas.add(createDM("39", "Thám tử", "Chọn 1 vùng 3 người, bạn sẽ biết có sói trong đó hay không"));
            datas.add(createDM("40", "Bị nguyền", "Bạn theo phe dân, nếu bị sói cắn sẽ thành sói"));
            datas.add(createDM("41", "Người bệnh", "Nếu sói/sát thủ cắn bạn, thì đêm sau sói/sát thủ không giết được ai"));
            datas.add(createDM("42", "Nhân bản", "Chọn 1 người, nếu người đó chết bạn sẽ nhận chức năng người đó"));
            datas.add(createDM("43", "Độc tài", "Duy nhất: chọn 1 người chơi, nếu không phải dân người đó chết, nếu dân bạn chết"));
            datas.add(createDM("44", "Thiên thần", "1 lần duy nhất có thể ngăn chặn toàn bộ cái chết trong đêm"));
            datas.add(createDM("45", "Phù thủy già", "mỗi ngày đuổi 1 người ko phải mình ra khỏi làng"));
            datas.add(createDM("46", "Boooooom", "Duy nhất chọn 1 người chơi giao bom, mỗi đêm bạn có quyền kích nổ hoặc ko"));
        }
    }

    private DataMember createDM(String id, String location, String description) {
        return DataMember.builder().id(id).location(location).description(description).build();
    }

    private DataMember createDM(DataMember d, Integer total) {
        return DataMember.builder().id(d.getId()).location(d.getLocation()).description(d.getDescription()).total(String.valueOf(total)).build();
    }
}

