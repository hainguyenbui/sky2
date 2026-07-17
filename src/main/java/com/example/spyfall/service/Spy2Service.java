package com.example.spyfall.service;

import com.example.spyfall.common.Spy2;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Spy2Service {

    private List<List<String>> keywords = new ArrayList<>();
    private List<Spy2> dataWillPlay = new ArrayList<>();
    @Getter
    private List<Spy2> dataPlayer = new ArrayList<>();
    @Getter
    private List<Spy2> removed = new ArrayList<>();
    private List<Spy2> dataMainSpy2 = new ArrayList<>();
    @Getter
    private final String image = "/spy2.png";
    @Getter
    private Integer countnumSpies = 0;
    @Getter
    private Integer countWhite = 0;
    @Getter
    private int numberGamePlay = 0;

    @Autowired
    private DataInputService dataInputService;

    public int getActivePlayerCount() {
        return dataPlayer.size() - removed.size();
    }

    /**
     * Get keyword for existing IP, or assign new one. Returns null if not set up.
     */
    public String getOrAssignKeyword(String deviceId, String name) {
        if (!dataPlayer.isEmpty()) {
            for (Spy2 dataMember : dataPlayer) {
                if (dataMember.getIpConfig().equals(deviceId)) {
                    if (!Objects.equals(dataMember.getUserName(), name)) {
                        dataMember.setUserName(name);
                    }
                    return dataMember.getKeyword();
                }
            }
        }
        if (dataWillPlay.isEmpty()) {
            if (numberGamePlay > 0) {
                return "";
            }
            return null; // not set up
        }
        Collections.shuffle(dataWillPlay);
        Spy2 yourLocation = dataWillPlay.get(0);
        yourLocation.setUserName(name);
        yourLocation.setIpConfig(deviceId);
        dataPlayer.add(yourLocation);
        dataWillPlay.remove(0);
        return yourLocation.getKeyword();
    }

    public void setupGame(int totalPlayers, int numSpies, int numWhite) {
        Random random = new Random();
        dataWillPlay.clear();
        dataMainSpy2.clear();
        dataPlayer.clear();
        removed.clear();
        numberGamePlay++;

        countWhite = numWhite;
        countnumSpies = numSpies;

        if (keywords.isEmpty()) {
            keywords.addAll(dataInputService.createDataSpy2());
        }
        checkDuplicatePairs();
        //get pair data spy-notSpy
        int dataget = random.nextInt(keywords.size());
        for (String spydata : keywords.get(dataget)) {
            dataMainSpy2.add(Spy2.builder().keyword(spydata).build());
        }
        keywords.remove(dataget);

        int indexMember = random.nextInt(dataMainSpy2.size());
        Spy2 dataNotSpy = dataMainSpy2.get(indexMember);
        Spy2 dataSpy = dataMainSpy2.get(1 - indexMember);

        int cnt = 1;
        for (int i = 0; i < totalPlayers - (numSpies + numWhite); i++) {
            dataWillPlay.add(createPlayer(cnt++, dataNotSpy.getKeyword(), "Dân Thường"));
        }
        for (int i = 0; i < numSpies; i++) {
            dataWillPlay.add(createPlayer(cnt++, dataSpy.getKeyword(), "Spy"));
        }
        for (int i = 0; i < numWhite; i++) {
            dataWillPlay.add(createPlayer(cnt++, "Bạn là mũ trắng", "Mũ Trắng"));
        }
    }

    public Spy2 removePlayer(int id) {
        for (Spy2 player : dataPlayer) {
            if (player.getId() == id) {
                player.setRemove(true);
                if (!removed.contains(player)) {
                    removed.add(player);
                }
                return player;
            }
        }
        return null;
    }

    private Spy2 createPlayer(int id, String keyword, String role) {
        return Spy2.builder().id(id).keyword(keyword).role(role).build();
    }

    private void checkDuplicatePairs() {
        Set<String> seen = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        for (List<String> pair : keywords) {
            String a = pair.get(0).trim();
            String b = pair.get(1).trim();
            String key = a.compareTo(b) < 0 ? a + "|" + b : b + "|" + a;
            if (seen.contains(key)) duplicates.add(key);
            else seen.add(key);
        }
        if (!duplicates.isEmpty()) {
            duplicates.forEach(dup -> System.out.println("Cặp lặp: " + dup.replace("|", " - ")));
        }
    }
}

