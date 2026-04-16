package com.dochiri.habitservice.domain.grass;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GrassLevelPolicy {

    public GrassLevel calculate(int completionCount) {

        if (completionCount <= 0) {
            return GrassLevel.NONE;
        }

        if (completionCount == 1) {
            return GrassLevel.LOW;
        }

        if (completionCount == 2) {
            return GrassLevel.MEDIUM;
        }

        if (completionCount <= 4) {
            return GrassLevel.HIGH;
        }

        return GrassLevel.FULL;
    }

}
