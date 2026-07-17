package com.example.spyfall.service;

import com.example.spyfall.common.Spy2;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
public class SpyService {

    @Getter
    private List<Spy2> keywords = new ArrayList<>();
    private List<Spy2> dataWillPlay = new ArrayList<>();
    @Getter
    private List<Spy2> dataPlayer = new ArrayList<>();
    private List<Spy2> listItemOfSpy = new ArrayList<>();
    @Getter
    private List<Spy2> removed = new ArrayList<>();
    @Getter
    private final String image = "/spy.png";
    @Getter
    private int numberGamePlay = 0;

    @Autowired
    DataInputService dataInputService;

    /**
     * Get or assign a member role for the given IP.
     * Returns null if game not set up yet.
     * Returns the location/role string.
     */
    public String getOrAssignMember(String deviceId, String name) {
        if (!dataPlayer.isEmpty()) {
            for (Spy2 dataMember : dataPlayer) {
                if (dataMember.getIpConfig().equals(deviceId)) {
                    return dataMember.getKeyword();
                }
            }
        }
        if (dataWillPlay.isEmpty()) {
            if (numberGamePlay >0) {
                return "";
            }
            return null; // not set up
        }
        Collections.shuffle(dataWillPlay);
        Spy2 yourLocation = dataWillPlay.get(0);
        yourLocation.setIpConfig(deviceId);
        yourLocation.setUserName(name);
        dataPlayer.add(yourLocation);
        dataWillPlay.remove(0);
        return yourLocation.getKeyword();
    }

    public boolean isGameSetup() {
        return !dataWillPlay.isEmpty() || !dataPlayer.isEmpty();
    }

    public void setupGame(int total, Integer spy, Integer whiteHat) {
        dataPlayer.clear();
        dataWillPlay.clear();
        listItemOfSpy.clear();
        numberGamePlay++;

        String[] datas = dataInputService.valueDataSpy().split(",");
        //clear keywords after
        if (ObjectUtils.isEmpty(keywords)) {
            for (String dataItem : datas) {
                String trimmed = dataItem.trim();
                boolean exist = keywords.stream().anyMatch(item -> item.getKeyword().equals(trimmed));
                if (!exist) {
                    int newId = keywords.size() + 1;
                    keywords.add(createDM(newId, trimmed, null));
                }
            }
        }

        Random random = new Random();

        listItemOfSpy = new ArrayList<>(keywords);
        int indexMember = random.nextInt(listItemOfSpy.size());
        Spy2 dataNotSpy = listItemOfSpy.get(indexMember);
        listItemOfSpy.remove(indexMember);

        int indexSpy = random.nextInt(listItemOfSpy.size());
        Spy2 dataSpy = listItemOfSpy.get(indexSpy);

        int totalWhiteHatAndSpy = whiteHat + spy;
        int cnt = 1;
        for (int i = 0; i < (total - totalWhiteHatAndSpy); i++) {
            dataWillPlay.add(createDM(cnt++, dataNotSpy.getKeyword(), "Dân Thường"));
        }
        for (int i = 0; i < spy; i++) {
            dataWillPlay.add(createDM(cnt++, dataSpy.getKeyword(), "Spy"));
        }
        for (int i = 0; i < whiteHat; i++) {
            dataWillPlay.add(createDM(cnt++, "Spy đó nhìn nhìn cái gì", "Mũ Trắng"));
        }
    }

    public boolean deleteItem(int id) {
        for (Spy2 item : keywords) {
            if (Objects.equals(item.getId(), id)) {
                keywords.remove(item);
                return true;
            }
        }
        return false;
    }

    public void addItem(String name) {
        int newId = keywords.stream()
                .mapToInt(Spy2::getId)
                .max().orElse(0) + 1;
        keywords.add(createDM(newId, name, null));
    }

    public Spy2 removePlayer(int id) {
        for (Spy2 player : dataPlayer) {
            if (Objects.equals(player.getId(), id)) {
                player.setRemove(true);
                if (!removed.contains(player)) {
                    removed.add(player);
                }
                return player;
            }
        }
        return null;
    }

    private Spy2 createDM(int id, String location, String role) {
        return Spy2.builder().id(id).keyword(location).role(role).build();
    }

}

