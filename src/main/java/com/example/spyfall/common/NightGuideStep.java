package com.example.spyfall.common;

import java.util.List;

public record NightGuideStep(
        String key,
        String icon,
        String title,
        String prompt,
        String sleepPrompt,
        int priority,
        List<NightGuideAction> actions) {

    public NightGuideStep {
        actions = List.copyOf(actions);
    }
}
