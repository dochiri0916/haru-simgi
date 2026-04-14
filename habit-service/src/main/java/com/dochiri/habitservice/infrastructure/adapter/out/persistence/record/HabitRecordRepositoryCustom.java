package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import java.time.Instant;
import java.util.List;

public interface HabitRecordRepositoryCustom {

    List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            String ownerType,
            String ownerPublicId,
            Instant fromDate,
            Instant toDate
    );

}
