package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.HabitRecordId;
import com.dochiri.habitservice.domain.record.exception.HabitRecordNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HabitRecordJpaAdapter implements HabitRecordRepository {

    private final HabitRecordJpaRepository habitRecordJpaRepository;

    @Override
    public HabitRecord save(HabitRecord record) {
        HabitRecordEntity entity = habitRecordJpaRepository.findByPublicId(record.getId().value())
                .map(existing -> {
                    HabitRecordMapper.updateEntity(record, existing);
                    return existing;
                })
                .orElseGet(() -> HabitRecordMapper.toEntity(record));

        HabitRecordEntity saved = habitRecordJpaRepository.save(entity);
        return HabitRecordMapper.toDomain(saved);
    }

    @Override
    public Optional<HabitRecord> findById(HabitRecordId id) {
        return habitRecordJpaRepository.findByPublicId(id.value())
                .map(HabitRecordMapper::toDomain);
    }

    @Override
    public HabitRecord loadById(HabitRecordId id) {
        return findById(id)
                .orElseThrow(() -> new HabitRecordNotFoundException(id));
    }

    @Override
    public Optional<HabitRecord> findByHabitIdAndCompletedAt(
            HabitId habitId,
            Instant completedAt
    ) {
        return habitRecordJpaRepository
                .findByHabitIdAndCompletedAt(habitId.value(), completedAt)
                .map(HabitRecordMapper::toDomain);
    }

    @Override
    public Optional<HabitRecord> findByHabitIdAndCompletedDate(
            HabitId habitId,
            LocalDate completedDate
    ) {
        return habitRecordJpaRepository
                .findByHabitIdAndCompletedDate(
                        habitId.value(),
                        completedDate
                )
                .map(HabitRecordMapper::toDomain);
    }

    @Override
    public List<HabitRecord> findByHabitIdAndCompletedAtBetween(
            HabitId habitId,
            Instant from,
            Instant to
    ) {
        return habitRecordJpaRepository
                .findByHabitIdAndCompletedAtBetween(habitId.value(), from, to)
                .stream()
                .map(HabitRecordMapper::toDomain)
                .toList();
    }

    @Override
    public List<HabitRecord> findByOwnerAndCompletedAtBetween(HabitOwner owner, Instant from, Instant to) {
        return habitRecordJpaRepository
                .findCompletionsForOwnerBetweenDates(
                        owner.type().name(),
                        owner.ownerId(),
                        from,
                        to
                )
                .stream()
                .map(HabitRecordMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(HabitRecordId id) {
        HabitRecordEntity entity = habitRecordJpaRepository.findByPublicId(id.value())
                .orElseThrow(() -> new HabitRecordNotFoundException(id));

        habitRecordJpaRepository.delete(entity);
    }

    @Override
    public void deleteByHabitId(HabitId habitId) {
        habitRecordJpaRepository.deleteByHabitId(habitId.value());
    }

}
