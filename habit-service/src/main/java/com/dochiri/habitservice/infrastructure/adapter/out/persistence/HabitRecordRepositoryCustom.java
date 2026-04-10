package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.List;

public interface HabitRecordRepositoryCustom {

    List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            String ownerType,
            String ownerReferenceId,
            Instant fromDate,
            Instant toDate
    );

}