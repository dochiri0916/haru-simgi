package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.habitservice.domain.habit.OwnerType;

import java.time.LocalDate;
import java.util.List;

public interface HabitRecordRepositoryCustom {

    List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            OwnerType ownerType,
            String ownerPublicId,
            LocalDate fromDate,
            LocalDate toDate
    );

}
