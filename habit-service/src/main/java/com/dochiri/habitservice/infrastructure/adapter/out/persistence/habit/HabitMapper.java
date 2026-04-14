package com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit;

import com.dochiri.habitservice.domain.habit.*;

public class HabitMapper {

    public static HabitEntity toEntity(Habit domain) {
        return new HabitEntity(
                domain.getId().value(),
                domain.getOwner().type(),
                domain.getOwner().ownerId(),
                domain.getName().value(),
                toColorType(domain.getColor()),
                domain.getIndex().value()
        );
    }

    public static Habit toDomain(HabitEntity entity) {
        HabitOwner owner = new HabitOwner(
                entity.getOwnerType(),
                entity.getOwnerPublicId()
        );

        return Habit.from(
                HabitId.of(entity.getPublicId()),
                owner,
                HabitName.of(entity.getName()),
                toDomainColor(entity.getColor()),
                HabitIndex.of(entity.getIndex()),
                entity.getCreatedAt()
        );
    }

    public static void updateEntity(Habit domain, HabitEntity entity) {
        entity.update(
                domain.getName().value(),
                toColorType(domain.getColor()),
                domain.getIndex().value()
        );
    }

    private static ColorType toColorType(HabitColor habitColor) {
        return ColorType.valueOf(habitColor.colorType().name());
    }

    private static HabitColor toDomainColor(ColorType colorType) {
        return HabitColor.of(ColorType.valueOf(colorType.name()));
    }

}
