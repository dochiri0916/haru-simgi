package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecordId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HabitRecordRepository {

    HabitRecord save(HabitRecord record);

    Optional<HabitRecord> findById(HabitRecordId id);

    HabitRecord loadById(HabitRecordId id);

    Optional<HabitRecord> findByHabitIdAndCompletedAt(HabitId habitId, Instant completedAt);

    Optional<HabitRecord> findByHabitIdAndCompletedDate(HabitId habitId, LocalDate completedDate);

    List<HabitRecord> findByHabitIdAndCompletedAtBetween(HabitId habitId, Instant from, Instant to);

    List<HabitRecord> findByOwnerAndCompletedAtBetween(HabitOwner owner, Instant from, Instant to);

    void delete(HabitRecordId id);

    void deleteByHabitId(HabitId habitId);

}
