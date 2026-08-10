package com.example.spyfall.common;

import java.util.List;

public record RoundHistory(String label, List<String> events) {

    public RoundHistory {
        events = List.copyOf(events);
    }
}
