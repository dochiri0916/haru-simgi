package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.HabitRecord;
import com.dochiri.habitservice.domain.HabitOwner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface HabitRecordRepository {

    HabitRecord save(HabitRecord record);

    Optional<HabitRecord> findById(String id);

    Optional<HabitRecord> findByHabitIdAndCompletedAt(String habitId, Instant completedAt);

    List<HabitRecord> findByHabitIdBetweenDates(String habitId, Instant fromDate, Instant toDate);

    List<HabitRecord> findByOwnerBetweenDates(HabitOwner owner, Instant fromDate, Instant toDate);

    void delete(String id);

    void deleteByHabitId(String habitId);

}
