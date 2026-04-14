package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.habit.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HabitTest {

    @Test
    void 이름을_변경해도_생성_시각은_유지된다() {
        Instant createdAt = Instant.parse("2026-04-14T00:00:00Z");
        Habit habit = Habit.create(
                HabitOwner.user("user-id"),
                HabitName.of("물 마시기"),
                HabitColor.ofDefault(),
                HabitIndex.of(0),
                createdAt
        );

        Habit renamed = habit.rename(HabitName.of("물 2L 마시기"));

        assertThat(renamed.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void 순서를_변경할_수_있다() {
        Habit habit = Habit.create(
                HabitOwner.user("user-id"),
                HabitName.of("물 마시기"),
                HabitColor.ofDefault(),
                HabitIndex.of(0),
                Instant.parse("2026-04-14T00:00:00Z")
        );

        Habit reordered = habit.reorder(HabitIndex.of(2));

        assertThat(reordered.getIndex().value()).isEqualTo(2);
    }

}
