package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.*;

public class HabitRecordMapper {

    public static HabitRecordEntity toEntity(HabitRecord domain) {
        return new HabitRecordEntity(
                domain.getId().value(),
                domain.getHabitId().value(),
                domain.getCompletedAt(),
                domain.isCompleted(),
                domain.getDuration() != null
                        ? domain.getDuration().minutes()
                        : null
        );
    }

    public static HabitRecord toDomain(HabitRecordEntity entity) {
        return HabitRecord.from(
                HabitRecordId.of(entity.getPublicId()),
                HabitId.of(entity.getHabitId()),
                entity.getCompletedAt(),
                entity.isCompleted(),
                entity.getDurationMinutes() != null
                        ? HabitDuration.of(entity.getDurationMinutes())
                        : null
        );
    }

}