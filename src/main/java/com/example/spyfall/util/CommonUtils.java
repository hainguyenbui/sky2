package com.example.spyfall.util;

import com.example.spyfall.common.Spy2;

import java.util.*;

public class CommonUtils {

    private static final Map<String, Integer> selectionCounts = new HashMap<>();
    private static final Random random = new Random();

    public static Integer addToList(String deviceId, List<Spy2> list2) {
        // Chỉ xét các object chưa có ID
        List<Spy2> emptyObjects = list2.stream()
                .filter(player -> player.getIpConfig() == null)
                .toList();

        if (emptyObjects.isEmpty()) {
            return null;
        }

        int count = selectionCounts.getOrDefault(deviceId, 0);
        int weight = Math.max(1, 10 - count);

        // Random theo trọng số giữa các object trống
        double totalWeight = emptyObjects.stream()
                .mapToDouble(player -> Objects.equals(player.getRole(), "Dân Thường") ? 10 : weight)
                .sum();

        double value = random.nextDouble() * totalWeight;

        for (int i = 0; i < emptyObjects.size(); i++) {
            Spy2 player = emptyObjects.get(i);
            value -= Objects.equals(player.getRole(), "Dân Thường") ? 10 : weight;

            if (value < 0) {
                if (!Objects.equals(player.getRole(), "Dân Thường")) {
                    selectionCounts.put(deviceId, count + 1);
                }

                return i;
            }
        }
        return null;
    }
}
