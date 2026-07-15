package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SpyService {

    private List<DataMember> dataPlay = new ArrayList<>();
    private List<DataMember> memberWillPlay = new ArrayList<>();
    private List<DataMember> memberPlay = new ArrayList<>();
    private List<DataMember> listItemOfSpy = new ArrayList<>();
    private DataMember allMemberData = null;
    private DataMember spyMemberData = null;
    private final String image = "/spy.png";

    private final String data2 = "Bệnh viện, Sân bay, Trường học, Trung Tâm giải trí, Rạp xiếc, " +
            "Ngân hàng, Trung tâm mua sắm, Khách sạn, Bãi biển, Sân vận động, Nhà tù, Quán cà phê, " +
            "Nhà hát, Bảo tàng, Thư viện, Khu cắm trại, Phòng gym, Ga tàu, Nhà thờ, Chùa, Nhà sách, " +
            "Rạp phim, Trung tâm tiệc cưới, Công viên nước, Thủy cung, Chợ đêm, Hiệu thuốc, Quán bar, " +
            "Karaoke, Nhà kho, Đồn công an, Phố đi bộ, Hồ bơi, Nhà hàng, Công viên, Sở thú";

    public String getImage() { return image; }

    public List<DataMember> getDataPlay() { return dataPlay; }

    /**
     * Get or assign a member role for the given IP.
     * Returns null if game not set up yet.
     * Returns the location/role string.
     */
    public String getOrAssignMember(String clientIp) {
        if (!memberPlay.isEmpty()) {
            for (DataMember dataMember : memberPlay) {
                if (dataMember.getIpData().equals(clientIp)) {
                    return dataMember.getLocation();
                }
            }
        }
        if (memberWillPlay.isEmpty()) {
            return null; // not set up
        }
        Collections.shuffle(memberWillPlay);
        DataMember yourLocation = memberWillPlay.get(0);
        yourLocation.setIpData(clientIp);
        memberPlay.add(yourLocation);
        memberWillPlay.remove(0);
        return yourLocation.getLocation();
    }

    public boolean isGameSetup() {
        return !memberWillPlay.isEmpty() || !memberPlay.isEmpty();
    }

    public void setupGame(int total, int style, Integer spy, Integer whiteHat) {
        memberPlay.clear();
        memberWillPlay.clear();
        listItemOfSpy.clear();
        dataPlay.clear();

        String[] datas = data2.split(",");
        for (String dataItem : datas) {
            String trimmed = dataItem.trim();
            boolean exist = dataPlay.stream().anyMatch(item -> item.getLocation().equals(trimmed));
            if (!exist) {
                int newId = dataPlay.size() + 1;
                dataPlay.add(createDM(newId, trimmed));
            }
        }

        Random random = new Random();
        int indexMember = random.nextInt(dataPlay.size());
        allMemberData = dataPlay.get(indexMember);

        listItemOfSpy = new ArrayList<>(dataPlay);
        listItemOfSpy.remove(indexMember);

        int indexSpy = random.nextInt(listItemOfSpy.size());
        spyMemberData = listItemOfSpy.get(indexSpy);

        if (spy == null) {
            for (int i = 1; i < total; i++) {
                memberWillPlay.add(createDM(total, allMemberData.getLocation()));
            }
            if (style == 1) {
                memberWillPlay.add(createDM(total, spyMemberData.getLocation()));
            } else {
                memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
            }
        } else {
            int whiteHatNum = (whiteHat == null) ? 0 : whiteHat;
            int totalWhiteHatAndSpy = whiteHatNum + spy;
            for (int i = 0; i < (total - totalWhiteHatAndSpy); i++) {
                memberWillPlay.add(createDM(total, allMemberData.getLocation()));
            }
            for (int i = 0; i < spy; i++) {
                if (style == 1) {
                    memberWillPlay.add(createDM(total, spyMemberData.getLocation()));
                } else {
                    memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
                }
            }
            for (int i = 0; i < whiteHatNum; i++) {
                memberWillPlay.add(createDM(total, "Spy đó nhìn nhìn cái gì"));
            }
        }
    }

    public boolean deleteItem(int id) {
        for (DataMember item : dataPlay) {
            if (item.getId().trim().equals(String.valueOf(id))) {
                dataPlay.remove(item);
                return true;
            }
        }
        return false;
    }

    public void addItem(String name) {
        int newId = dataPlay.stream()
                .mapToInt(o -> { try { return Integer.parseInt(o.getId()); } catch (NumberFormatException e) { return 0; } })
                .max().orElse(0) + 1;
        dataPlay.add(createDM(newId, name));
    }

    private DataMember createDM(int id, String location) {
        return DataMember.builder().id(String.valueOf(id)).location(location).build();
    }
}

