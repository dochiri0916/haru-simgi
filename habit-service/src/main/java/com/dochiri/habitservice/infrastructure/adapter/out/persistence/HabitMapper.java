package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.OwnerType;

public class HabitMapper {

    public static HabitEntity toEntity(Habit habit) {
        return HabitEntity.create(
                habit.getId().value(),
                habit.getOwner().type().name(),
                habit.getOwner().referenceId(),
                habit.getName().value(),
                habit.getInvestedMinutes()
        );
    }

    public static Habit toDomain(HabitEntity entity) {
        OwnerType ownerType = OwnerType.valueOf(entity.getOwnerType());
        HabitOwner owner = new HabitOwner(ownerType, entity.getOwnerReferenceId());

        return Habit.from(
                entity.getPublicId(),
                owner,
                entity.getName(),
                entity.getInvestedMinutes()
        );
    }

}