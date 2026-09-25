package com.example.spyfall.service;

import com.example.spyfall.common.DataMember;
import com.example.spyfall.common.SettingDto;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MaSoiAutoService {
    private final MaSoiService maSoiService;
    private final Object lock = new Object();
    private final Map<String, Map<String, Integer>> deviceIdOrder = new HashMap<>();
    private final Map<String, SettingDto> settingsByDeviceId = new LinkedHashMap<>();

    public MaSoiAutoService(MaSoiService maSoiService) {
        this.maSoiService = maSoiService;
    }

    public Map<String, Object> getViewerOrderConfig(String viewerDeviceId) {
        synchronized (lock) {
            Map<String, Object> result = new LinkedHashMap<>();
            if (ObjectUtils.isEmpty(viewerDeviceId)) {
                result.put("deviceOrder", Map.of());
                result.put("players", orderPlayersView());
                return result;
            }
            result.put("deviceOrder", new LinkedHashMap<>(viewerOrderMap(viewerDeviceId)));
            result.put("players", orderPlayersView());
            return result;
        }
    }

    public Map<String, Object> updateViewerOrder(String viewerDeviceId, Map<String, Integer> requestedOrder) {
        synchronized (lock) {
            if (ObjectUtils.isEmpty(viewerDeviceId)) {
                return errorState("Thiếu thông tin người chơi");
            }
            Map<String, Integer> normalized = normalizeViewerOrder(requestedOrder);
            if (normalized.isEmpty()) {
                deviceIdOrder.remove(viewerDeviceId);
            } else {
                deviceIdOrder.put(viewerDeviceId, normalized);
            }
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("ok", true);
            result.put("deviceOrder", new LinkedHashMap<>(viewerOrderMap(viewerDeviceId)));
            result.put("players", orderPlayersView());
            return result;
        }
    }

    public SettingDto updateSetting(String deviceId, SettingDto settingDto) {
        synchronized (lock) {
            if (ObjectUtils.isEmpty(deviceId)) {
                SettingDto fallback = settingDto == null ? defaultSetting() : settingDto;
                fallback.normalize();
                return cloneSetting(fallback);
            }
            SettingDto normalized = settingDto == null ? defaultSetting() : settingDto;
            normalized.normalize();
            settingsByDeviceId.put(deviceId, cloneSetting(normalized));
            return cloneSetting(settingsByDeviceId.get(deviceId));
        }
    }

    public SettingDto resolveViewerSetting(String viewerDeviceId) {
        if (ObjectUtils.isEmpty(viewerDeviceId)) {
            return defaultSetting();
        }
        return settingsByDeviceId.computeIfAbsent(viewerDeviceId, key -> defaultSetting());
    }

    private SettingDto defaultSetting() {
        return new SettingDto(false, false);
    }

    public SettingDto cloneSetting(SettingDto source) {
        if (source == null) {
            return defaultSetting();
        }
        return new SettingDto(source.isChatOn(), source.isFunctionOn());
    }

    public SettingDto getSetting(String deviceId) {
        synchronized (lock) {
            return cloneSetting(resolveViewerSetting(deviceId));
        }
    }

    private Map<String, Object> errorState(String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("ok", false);
        payload.put("message", message);
        return payload;
    }

    private Map<String, Integer> viewerOrderMap(String viewerDeviceId) {
        Map<String, Integer> order = deviceIdOrder.get(viewerDeviceId);
        if (order == null) {
            return Map.of();
        }
        return normalizeViewerOrder(order);
    }

    private Map<String, Integer> normalizeViewerOrder(Map<String, Integer> requestedOrder) {
        if (requestedOrder == null || requestedOrder.isEmpty()) {
            return Map.of();
        }
        List<String> baseOrder = orderedAssignedDeviceIds();
        Map<String, Integer> baseIndex = new HashMap<>();
        for (int i = 0; i < baseOrder.size(); i++) {
            baseIndex.put(baseOrder.get(i), i);
        }
        List<Map.Entry<String, Integer>> prioritized = requestedOrder.entrySet().stream()
                .filter(entry -> !ObjectUtils.isEmpty(entry.getKey()) && entry.getValue() != null && baseIndex.containsKey(entry.getKey()))
                .sorted(Comparator
                        .comparingInt(Map.Entry<String, Integer>::getValue)
                        .thenComparingInt(entry -> baseIndex.getOrDefault(entry.getKey(), Integer.MAX_VALUE)))
                .toList();

        LinkedHashMap<String, Integer> normalized = new LinkedHashMap<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : prioritized) {
            if (!normalized.containsKey(entry.getKey())) {
                normalized.put(entry.getKey(), index++);
            }
        }
        for (String deviceId : baseOrder) {
            if (!normalized.containsKey(deviceId)) {
                normalized.put(deviceId, index++);
            }
        }
        return normalized;
    }

    private List<String> orderedAssignedDeviceIds() {
        List<String> ordered = new ArrayList<>();
        for (DataMember player : maSoiService.getPls()) {
            if (!ObjectUtils.isEmpty(player.getIpData())) {
                ordered.add(player.getIpData());
            }
        }
        return ordered;
    }

    private List<Map<String, Object>> orderPlayersView() {
        List<Map<String, Object>> players = new ArrayList<>();
        for (DataMember player : maSoiService.getPls()) {
            if (ObjectUtils.isEmpty(player.getIpData())) {
                continue;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("deviceId", player.getIpData());
            item.put("name", player.getNameMember() != null ? player.getNameMember() : "Ẩn danh");
            item.put("isDead", player.isDead());
            players.add(item);
        }
        return players;
    }
}
