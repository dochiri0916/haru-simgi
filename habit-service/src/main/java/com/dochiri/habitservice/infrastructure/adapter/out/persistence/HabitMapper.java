package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.*;

public class HabitMapper {

    public static HabitEntity toEntity(Habit domain) {
        return new HabitEntity(
                domain.getId().value(),
                domain.getOwner().type(),
                domain.getOwner().referenceId(),
                domain.getName().value()
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