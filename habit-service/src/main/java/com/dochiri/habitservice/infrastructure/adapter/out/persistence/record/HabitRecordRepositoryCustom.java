package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import java.time.LocalDate;
import java.util.List;

public interface HabitRecordRepositoryCustom {

    List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            String ownerType,
            String ownerPublicId,
            LocalDate fromDate,
            LocalDate toDate
    );

}
