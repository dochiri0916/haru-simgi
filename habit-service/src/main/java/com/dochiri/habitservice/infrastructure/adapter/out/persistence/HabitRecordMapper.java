package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.HabitRecord;

public class HabitRecordMapper {

    public static HabitRecordEntity toEntity(HabitRecord domain) {
        return HabitRecordEntity.create(
                domain.getId().value(),
                domain.getHabitId().value(),
                domain.getCompletedAt(),
                domain.getValue()
        );
    }

    public static HabitRecord toDomain(HabitRecordEntity entity) {
        return HabitRecord.from(
                entity.getPublicId(),
                entity.getHabitId(),
                entity.getCompletedAt(),
                entity.getValue()
        );
    }

}
