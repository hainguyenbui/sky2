package com.example.spyfall.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SettingDto {
    private boolean chatOn;
    private boolean functionOn;

    public SettingDto(boolean chatOn, boolean functionOn) {
        this.chatOn = chatOn;
        this.functionOn = functionOn;
    }

    public void normalize() {
        // no-op: giữ method để tương thích luồng cũ trong service
    }
}
