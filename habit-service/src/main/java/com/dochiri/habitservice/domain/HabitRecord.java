package com.dochiri.habitservice.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HabitRecord {

    private final HabitRecordId id;
    private final HabitId habitId;
    private final Instant completedAt;
    private final HabitRecordValue value;

    public static HabitRecord create(HabitId habitId, Instant completedAt, int value) {
        return new HabitRecord(
                HabitRecordId.newId(),
                habitId,
                completedAt,
                HabitRecordValue.of(value)
        );
    }

    public static HabitRecord from(HabitRecordId id, HabitId habitId, Instant completedAt, HabitRecordValue value) {
        return new HabitRecord(
                id,
                habitId,
                completedAt,
                value
        );
    }

}