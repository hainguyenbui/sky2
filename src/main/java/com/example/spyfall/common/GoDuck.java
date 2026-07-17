package com.example.spyfall.common;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GoDuck {
    Integer id;
    String deviceId;
    String userName;
    List<Integer> numberSee;

    @Override
    public String toString() {
        return id + ". " + userName + ". ";
    }
}
