package com.dochiri.habitservice.domain;

public enum GrassLevel {

    NONE(0), LOW(1), MEDIUM(2), HIGH(3), FULL(4);

    private final int level;

    GrassLevel(int level) {
        this.level = level;
    }

    public int level() {
        return level;
    }

    public static GrassLevel from(int completionCount) {
        if (completionCount == 0) return NONE;
        if (completionCount == 1) return LOW;
        if (completionCount == 2) return MEDIUM;
        if (completionCount <= 4) return HIGH;
        return FULL;
    }

}
