package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameSetupRequest {
    private int totalPlayers;
    private boolean soiNguyenEnabled;
    private boolean cupidEnabled;
    @Builder.Default
    private Map<Integer, Integer> roleCounts = new HashMap<>();

    public int getRoleCount(int roleId) {
        return roleCounts.getOrDefault(roleId, 0);
    }

    public static GameSetupRequest fromQueryParams(Map<String, String> params) {
        Map<Integer, Integer> parsedRoleCounts = new HashMap<>();
        params.forEach((key, value) -> {
            if (isIntegerKey(key)) {
                parsedRoleCounts.put(Integer.parseInt(key), parseInt(value));
            }
        });

        return GameSetupRequest.builder()
                .totalPlayers(parseInt(params.get("total")))
                .soiNguyenEnabled(Boolean.parseBoolean(params.get("checkSoi")))
                .cupidEnabled(Boolean.parseBoolean(params.get("checkCupid")))
                .roleCounts(parsedRoleCounts)
                .build();
    }

    private static boolean isIntegerKey(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static int parseInt(String value) {
        if (ObjectUtils.isEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
