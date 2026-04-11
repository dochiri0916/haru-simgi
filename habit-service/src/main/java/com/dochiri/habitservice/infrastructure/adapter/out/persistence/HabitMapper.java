package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.*;

public class HabitMapper {

    public static HabitEntity toEntity(Habit habit) {
        return new HabitEntity(
                habit.getId().value(),
                habit.getOwner().type(),
                habit.getOwner().referenceId(),
                habit.getName().value()
        );
    }

    public static Habit toDomain(HabitEntity entity) {
        HabitOwner owner = new HabitOwner(
                entity.getOwnerType(),
                entity.getOwnerReferenceId()
        );

        return Habit.from(
                HabitId.of(entity.getPublicId()),
                owner,
                HabitName.of(entity.getName())
        );
    }

}