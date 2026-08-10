package com.example.spyfall.common;

public record GameSessionState(
        long currentSessionId,
        String lifecycle,
        boolean active,
        boolean requestedSessionDiscarded,
        boolean requestedSessionFinished,
        boolean replacementReady) {
}
