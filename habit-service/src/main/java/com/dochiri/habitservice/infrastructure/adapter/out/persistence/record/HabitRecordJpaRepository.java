package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface HabitRecordJpaRepository extends JpaRepository<HabitRecordEntity, Long>, HabitRecordRepositoryCustom {

    Optional<HabitRecordEntity> findByPublicId(String publicId);

    Optional<HabitRecordEntity> findByHabitIdAndCompletedAt(String habitId, Instant completedAt);

    List<HabitRecordEntity> findByHabitIdAndCompletedAtBetween(String habitId, Instant fromDate, Instant toDate);

    void deleteByHabitId(String habitId);

}