package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.record.HabitDuration;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.HabitRecordId;

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