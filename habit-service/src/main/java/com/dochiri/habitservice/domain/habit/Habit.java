package com.dochiri.habitservice.domain.habit;

import com.dochiri.habitservice.domain.record.HabitCompletion;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.habit.exception.HabitAccessDeniedException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.Instant;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Habit {

    private final HabitId id;
    private final HabitOwner owner;
    private final HabitName name;
    private final HabitColor color;
    private final HabitIndex index;
    private final Instant createdAt;

    public static Habit create(HabitOwner owner, HabitName name, HabitColor color, HabitIndex index, Instant createdAt) {
        return new Habit(
                HabitId.newId(),
                owner,
                name,
                color,
                index,
                createdAt
        );
    }

    public static Habit from(HabitId id, HabitOwner owner, HabitName name, HabitColor color, HabitIndex index, Instant createdAt) {
        return new Habit(
                id,
                owner,
                name,
                color,
                index,
                createdAt
        );
    }

    public void assertOwner(HabitOwner requestOwner) {
        if (!this.owner.equals(requestOwner)) {
            throw new HabitAccessDeniedException(
                    this.id,
                    this.owner,
                    requestOwner
            );
        }
    }

    public Habit rename(HabitName newName) {
        if (this.name.equals(newName)) {
            return this;
        }

        return new Habit(
                this.id,
                this.owner,
                newName,
                this.color,
                this.index,
                this.createdAt
        );
    }

    public Habit reorder(HabitIndex newIndex) {
        if (this.index.equals(newIndex)) {
            return this;
        }

        return new Habit(
                this.id,
                this.owner,
                this.name,
                this.color,
                newIndex,
                this.createdAt
        );
    }

    public HabitRecord complete(HabitCompletion completion) {
        return HabitRecord.create(
                this.id,
                completion
        );
    }

}
