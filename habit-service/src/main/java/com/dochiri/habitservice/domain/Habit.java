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
    private final Integer investedMinutes;

    public static Habit create(HabitOwner owner, String name) {
        return new Habit(
                HabitId.newId(),
                owner,
                HabitName.of(name),
                null
        );
    }

    public static Habit from(String id, HabitOwner owner, String name, Integer investedMinutes) {
        return new Habit(
                HabitId.of(id),
                owner,
                HabitName.of(name),
                investedMinutes
        );
    }

    public void validateOwner(HabitOwner requestOwner) {
        if (!this.owner.equals(requestOwner)) {
            throw new HabitAccessDeniedException();
        }
    }

    public Habit rename(String newName) {
        HabitName newHabitName = HabitName.of(newName);

        if (this.name.equals(newHabitName)) {
            return this;
        }

        return new Habit(
                id,
                owner,
                newHabitName,
                investedMinutes
        );
    }

}