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
    private final boolean completed;
    private final HabitDuration duration;

    public static HabitRecord create(HabitId habitId, HabitCompletion completion) {
        return new HabitRecord(
                HabitRecordId.newId(),
                habitId,
                completion.completedAt(),
                true,
                completion.duration()
        );
    }

    public static HabitRecord from(HabitRecordId id, HabitId habitId, Instant completedAt, boolean completed, HabitDuration duration) {
        return new HabitRecord(
                id,
                habitId,
                completedAt,
                completed,
                duration
        );
    }

    public boolean hasDuration() {
        return duration != null;
    }

}