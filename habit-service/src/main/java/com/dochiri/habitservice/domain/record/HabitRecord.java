package com.dochiri.habitservice.domain.record;

import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.record.exception.InvalidCompletedAtException;
import lombok.Getter;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Getter
public final class HabitRecord {

    private final HabitRecordId id;
    private final HabitId habitId;
    private final Instant completedAt;
    private final HabitDuration duration;
    private final HabitMemo memo;

    private HabitRecord(HabitRecordId id, HabitId habitId, Instant completedAt, HabitDuration duration, HabitMemo memo) {
        this.id = requireNonNull(id);
        this.habitId = requireNonNull(habitId);
        this.completedAt = requireNonNull(completedAt);
        this.duration = duration;
        this.memo = requireNonNull(memo);
    }

    public static HabitRecord create(HabitId habitId, Instant completedAt, Integer minutes, String memo) {
        return new HabitRecord(
                HabitRecordId.newId(),
                habitId,
                requireCompletedAt(completedAt),
                minutes == null ? null : HabitDuration.of(minutes),
                HabitMemo.of(memo)
        );
    }

    public static HabitRecord from(HabitRecordId id, HabitId habitId, Instant completedAt, HabitDuration duration, HabitMemo memo) {
        return new HabitRecord(
                id,
                habitId,
                completedAt,
                duration,
                memo
        );
    }

    public HabitRecord update(Instant completedAt, Integer minutes, String memo) {
        return new HabitRecord(
                this.id,
                this.habitId,
                requireCompletedAt(completedAt),
                minutes == null ? null : HabitDuration.of(minutes),
                HabitMemo.of(memo)
        );
    }

    public boolean hasDuration() {
        return duration != null;
    }

    private static Instant requireCompletedAt(Instant completedAt) {
        if (completedAt == null) {
            throw new InvalidCompletedAtException();
        }
        return completedAt;
    }

}
