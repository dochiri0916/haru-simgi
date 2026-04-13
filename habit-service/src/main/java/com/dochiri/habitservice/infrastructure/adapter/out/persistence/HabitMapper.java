package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.*;

public class HabitMapper {

    public static HabitEntity toEntity(Habit domain) {
        return new HabitEntity(
                domain.getId().value(),
                domain.getOwner().type(),
                domain.getOwner().referenceId(),
                domain.getName().value(),
                toColorType(domain.getColor())
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
                HabitName.of(entity.getName()),
                toDomainColor(entity.getColor())
        );
    }

    private static HabitColorType toColorType(HabitColor habitColor) {
        return HabitColorType.valueOf(habitColor.colorType().name());
    }

    private static HabitColor toDomainColor(HabitColorType colorType) {
        return HabitColor.of(HabitColor.ColorType.valueOf(colorType.name()));
    }

}