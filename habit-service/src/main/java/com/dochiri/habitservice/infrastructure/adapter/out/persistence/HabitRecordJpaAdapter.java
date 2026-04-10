package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.domain.HabitRecord;
import com.dochiri.habitservice.domain.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HabitRecordJpaAdapter implements HabitRecordRepository {

    private final HabitRecordJpaRepository habitRecordJpaRepository;

    @Override
    public HabitRecord save(HabitRecord record) {
        HabitRecordEntity entity = HabitRecordMapper.toEntity(record);
        HabitRecordEntity saved = habitRecordJpaRepository.save(entity);
        return HabitRecordMapper.toDomain(saved);
    }

    @Override
    public Optional<HabitRecord> findById(String id) {
        return habitRecordJpaRepository.findByPublicId(id)
                .map(HabitRecordMapper::toDomain);
    }

    @Override
    public Optional<HabitRecord> findByHabitIdAndCompletedAt(String habitId, Instant completedAt) {
        return habitRecordJpaRepository.findByHabitIdAndCompletedAt(habitId, completedAt)
                .map(HabitRecordMapper::toDomain);
    }

    @Override
    public List<HabitRecord> findByHabitIdBetweenDates(String habitId, Instant fromDate, Instant toDate) {
        return habitRecordJpaRepository.findByHabitIdAndCompletedAtBetween(habitId, fromDate, toDate)
                .stream()
                .map(HabitRecordMapper::toDomain)
                .toList();
    }

    @Override
    public List<HabitRecord> findByOwnerBetweenDates(HabitOwner owner, Instant fromDate, Instant toDate) {
        return habitRecordJpaRepository.findCompletionsForOwnerBetweenDates(owner.type().name(), owner.referenceId(), fromDate, toDate)
                .stream()
                .map(HabitRecordMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(String id) {
        habitRecordJpaRepository.findByPublicId(id)
                .ifPresent(habitRecordJpaRepository::delete);
    }

    @Override
    public void deleteByHabitId(String habitId) {
        habitRecordJpaRepository.deleteByHabitId(habitId);
    }

}