package com.dochiri.habitservice.domain;

import java.time.Instant;

public record HabitCompletion(
        Instant completed
) {
}
