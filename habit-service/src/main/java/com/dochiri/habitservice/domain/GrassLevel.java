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

    public static GrassLevel from(int investedMinutes) {
        if (investedMinutes == 0) return NONE;
        if (investedMinutes < 30) return LOW;
        if (investedMinutes < 60) return MEDIUM;
        if (investedMinutes < 120) return HIGH;
        return FULL;
    }

}
