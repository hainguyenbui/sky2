package com.example.spyfall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoSelectRequest {
    private String actorDeviceId;
    private String targetDeviceId;
    private String colType;
    private String connValue;
}
