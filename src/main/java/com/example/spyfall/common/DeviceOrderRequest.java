package com.example.spyfall.common;

import lombok.Data;

import java.util.Map;

@Data
public class DeviceOrderRequest {
    private Map<String, Integer> deviceOrder;
}
