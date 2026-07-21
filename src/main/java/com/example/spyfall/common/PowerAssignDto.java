package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PowerAssignDto {
    private String deviceId;
    private String roleId;
    private String roleName;
    private String skillType;
    private boolean enabled;
}
