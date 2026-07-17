package com.example.spyfall.service;

import com.example.spyfall.common.GoDuck;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoDuckService {

    private Map<String, GoDuck> memberPlay = new HashMap<>();
    private List<GoDuck> dataPlay = new ArrayList<>();
    @Getter
    private Integer totalPlay = 0;
    @Getter
    private Integer spyTotalPlay = 0;
    @Getter
    private final String image = "/gDuck.png";
    @Getter
    private int countGame = 0;
    List<Integer> spyPlayers = new ArrayList<>();
    List<Integer> notSpyPlayers = new ArrayList<>();

    public List<GoDuck> getSortedMembers() {
        return memberPlay.values().stream()
                .sorted(Comparator.comparing(GoDuck::getId))
                .toList();
    }

    public String getOrRegisterMember(String deviceId, String name) {
        if (ObjectUtils.isEmpty(dataPlay)) {
            return null;
        }
        if (memberPlay.containsKey(deviceId)) {
            memberPlay.get(deviceId).setUserName(name);
        } else {
            int idMember = memberPlay.size() + 1;
            GoDuck data = dataPlay.stream().filter(d -> d.getId().equals(idMember)).findFirst().orElse(null);

            if (!ObjectUtils.isEmpty(data)) {
                data.setDeviceId(deviceId);
                data.setUserName(name);
            }
            memberPlay.put(deviceId, data);
        }
        return memberPlay.get(deviceId).getNumberSee()
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
    }

    public void setupGame(int total, int spy) {
        spyPlayers.clear();
        notSpyPlayers.clear();
        totalPlay = total;
        spyTotalPlay = spy;
        countGame++;
        memberPlay.forEach((k, v) -> {
            v.setNumberSee(new ArrayList<>());
        });

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalPlay; i++) numbers.add(i);

        Collections.shuffle(numbers);

        spyPlayers.addAll(numbers.subList(0, spy));
        notSpyPlayers.addAll(numbers.subList(spy, spy*2));
        for (int i = dataPlay.size() + 1; i <= totalPlay; i++) {
            GoDuck member = GoDuck.builder().id(i).build();
            dataPlay.add(member);
        }
        dataPlay.forEach(d -> {
            if (spyPlayers.contains(d.getId())) {
                d.setNumberSee(notSpyPlayers);
            } else {
                d.setNumberSee(spyPlayers);
            }
        });
    }

    public void clear() {
        memberPlay.clear();
        spyPlayers.clear();
        notSpyPlayers.clear();
        totalPlay = 0;
        spyTotalPlay = 0;
        countGame = 0;
    }
}

