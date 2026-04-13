package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.exception.HabitAccessDeniedException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Habit {

    private final HabitId id;
    private final HabitOwner owner;
    private final HabitName name;
    private final HabitColor color;

    public static Habit create(HabitOwner owner, HabitName name, HabitColor color) {
        return new Habit(
                HabitId.newId(),
                owner,
                name,
                color
        );
    }

    public static Habit from(HabitId id, HabitOwner owner, HabitName name, HabitColor color) {
        return new Habit(
                id,
                owner,
                name,
                color
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
                this.color
        );
    }

    public HabitRecord complete(HabitCompletion completion) {
        return HabitRecord.create(
                this.id,
                completion
        );
    }

}