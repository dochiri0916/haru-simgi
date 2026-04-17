package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.habit.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class HabitTest {

    @Test
    @DisplayName("이름을 변경해도 생성 시각은 유지된다")
    void keepsCreatedAtWhenRenamed() {
        // given
        Instant createdAt = Instant.parse("2026-04-14T00:00:00Z");
        Habit habit = Habit.create(
                HabitOwner.user("user-id"),
                HabitName.of("물 마시기"),
                HabitColor.of(ColorType.GREEN),
                HabitIndex.of(0),
                createdAt
        );

        // when
        Habit renamed = habit.rename(HabitName.of("물 2L 마시기"));

        // then
        assertThat(renamed.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("순서를 변경할 수 있다")
    void reordersHabit() {
        // given
        Habit habit = Habit.create(
                HabitOwner.user("user-id"),
                HabitName.of("물 마시기"),
                HabitColor.of(ColorType.GREEN),
                HabitIndex.of(0),
                Instant.parse("2026-04-14T00:00:00Z")
        );

        // when
        Habit reordered = habit.reorder(HabitIndex.of(2));

        // then
        assertThat(reordered.getIndex().value()).isEqualTo(2);
    }

}
