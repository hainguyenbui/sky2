package com.example.spyfall.common;

public record NightGuideAction(
        String key,
        String type,
        String icon,
        String label,
        int roleId,
        String roleName,
        String actorDeviceId,
        String connectionValue,
        boolean disabled,
        String targetPolicy,
        boolean toggleOnly) {
}
