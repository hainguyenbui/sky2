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
        boolean toggleOnly,
        /** Động từ dùng ở bước tổng kết: "Sói <b>cắn</b> Hà 5" thay vì "Sói → Hà 5". */
        String verb) {
}
