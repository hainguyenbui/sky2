package com.example.spyfall.service;

import com.example.spyfall.common.GoDuck;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoDuckService {

    private Map<String, GoDuck> memberPlay = new HashMap<>();
    private Map<Integer, GoDuck> spyMember = new HashMap<>();
    private Map<Integer, GoDuck> spySeeMember = new HashMap<>();
    private Integer totalPlay = 0;
    private Integer spyTotalPlay = 0;
    private final String image = "/gDuck.png";

    public String getImage() { return image; }
    public Integer getTotalPlay() { return totalPlay; }
    public Integer getSpyTotalPlay() { return spyTotalPlay; }

    public List<GoDuck> getSortedMembers() {
        return memberPlay.values().stream()
                .sorted(Comparator.comparing(GoDuck::getId))
                .toList();
    }

    public GoDuck getOrRegisterMember(String clientIp, String name) {
        // Update spy/spySee maps with current names
        memberPlay.values().forEach(item -> {
            if (spyMember.containsKey(item.getId())) spyMember.put(item.getId(), item);
            else if (spySeeMember.containsKey(item.getId())) spySeeMember.put(item.getId(), item);
        });

        if (memberPlay.containsKey(clientIp)) {
            memberPlay.get(clientIp).setUserName(name);
        } else {
            int idMember = memberPlay.size() + 1;
            memberPlay.put(clientIp, GoDuck.builder().ipConfig(clientIp).userName(name).id(idMember).build());
        }
        return memberPlay.get(clientIp);
    }

    public String buildSecretInfo(String clientIp) {
        GoDuck member = memberPlay.get(clientIp);
        if (member == null) return "";
        Integer idMemberPlay = member.getId();
        StringBuilder sb = new StringBuilder();
        Map<Integer, GoDuck> targetMap = spyMember.containsKey(idMemberPlay) ? spySeeMember : spyMember;
        for (Map.Entry<Integer, GoDuck> entry : targetMap.entrySet()) {
            if (entry.getValue() != null) {
                sb.append(entry.getValue().getUserName()).append(" (ID: ").append(entry.getKey()).append(")");
            } else {
                sb.append("ID: ").append(entry.getKey());
            }
        }
        return sb.toString();
    }

    public void setupGame(int total, int spy) {
        spyMember.clear();
        spySeeMember.clear();
        memberPlay.clear();
        totalPlay = total;
        spyTotalPlay = spy;

        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= totalPlay; i++) numbers.add(i);
        Collections.shuffle(numbers);

        for (int i = 0; i < spyTotalPlay; i++) {
            spyMember.put(numbers.remove(0), null);
            Collections.shuffle(numbers);
        }
        for (int i = 0; i < spyTotalPlay; i++) {
            spySeeMember.put(numbers.remove(0), null);
            Collections.shuffle(numbers);
        }
    }
}

