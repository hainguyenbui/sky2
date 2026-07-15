package com.example.spyfall.service;

import com.example.spyfall.common.Spy2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Spy2Service {

    private List<List<String>> keywords = new ArrayList<>();
    private List<Spy2> dataWillPlay = new ArrayList<>();
    private List<Spy2> dataPlayer = new ArrayList<>();
    private List<Spy2> dataSpies = new ArrayList<>();
    private List<Spy2> removed = new ArrayList<>();
    private List<Spy2> dataMainSpy2 = new ArrayList<>();
    private Spy2 dataSpy;
    private Spy2 dataNotSpy;
    private final String image = "/spy2.png";
    private Integer countnumSpies = 0;
    private Integer countWhite = 0;

    @Autowired
    private DataInputService dataInputService;

    public String getImage() { return image; }
    public Integer getCountnumSpies() { return countnumSpies; }
    public Integer getCountWhite() { return countWhite; }
    public List<Spy2> getRemoved() { return removed; }
    public List<Spy2> getDataPlayer() { return dataPlayer; }

    public int getActivePlayerCount() {
        return dataPlayer.size() - removed.size();
    }

    /**
     * Get keyword for existing IP, or assign new one. Returns null if not set up.
     */
    public String getOrAssignKeyword(String clientIp, String name) {
        if (!dataPlayer.isEmpty()) {
            for (Spy2 dataMember : dataPlayer) {
                if (dataMember.getIpConfig().equals(clientIp)) {
                    if (!Objects.equals(dataMember.getUserName(), name)) {
                        dataMember.setUserName(name);
                    }
                    return dataMember.getKeyword();
                }
            }
        }
        if (dataWillPlay.isEmpty()) {
            return null;
        }
        Collections.shuffle(dataWillPlay);
        Spy2 yourLocation = dataWillPlay.get(0);
        yourLocation.setUserName(name);
        yourLocation.setIpConfig(clientIp);
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

        if (keywords.isEmpty()) {
            keywords.addAll(dataInputService.createDataSpy2());
        }
        checkDuplicatePairs();
        int dataget = random.nextInt(keywords.size());
        for (String spydata : keywords.get(dataget)) {
            dataMainSpy2.add(Spy2.builder().keyword(spydata).build());
        }
        keywords.remove(dataget);

        countnumSpies = numSpies;
        countWhite = numWhite;

        int indexMember = random.nextInt(dataMainSpy2.size());
        dataNotSpy = dataMainSpy2.get(indexMember);
        dataSpies = new ArrayList<>(dataMainSpy2);
        dataSpies.remove(indexMember);
        int indexSpy = random.nextInt(dataSpies.size());
        dataSpy = dataSpies.get(indexSpy);

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

    public void removePlayer(int id) {
        for (Spy2 player : dataPlayer) {
            if (player.getId() == id) {
                player.setRemove(true);
                if (!removed.contains(player)) {
                    removed.add(player);
                }
            }
        }
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

