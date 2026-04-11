package com.dochiri.habitservice.domain;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GrassLevelPolicy {

    public GrassLevel calculate(int minutes) {

        if (minutes <= 0) {
            return GrassLevel.NONE;
        }

        if (minutes < 30) {
            return GrassLevel.LOW;
        }

        if (minutes < 60) {
            return GrassLevel.MEDIUM;
        }

        if (minutes < 120) {
            return GrassLevel.HIGH;
        }

        return GrassLevel.FULL;
    }

}