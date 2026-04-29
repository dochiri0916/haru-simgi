package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.habitservice.application.port.out.HabitGrassAggregation;
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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public HabitRecord loadById(HabitRecordId id) {
        return habitRecordJpaRepository.findByPublicId(id.value())
                .map(HabitRecordMapper::toDomain)
                .orElseThrow(() -> new HabitRecordNotFoundException(id));
    }

    @Override
    public List<HabitRecord> findByHabitId(HabitId habitId) {
        return habitRecordJpaRepository
                .findByHabitId(habitId.value())
                .stream()
                .map(HabitRecordMapper::toDomain)
                .toList();
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
    public Map<LocalDate, HabitGrassAggregation> aggregateGrassByOwnerAndCompletedDateBetween(
            HabitOwner owner,
            LocalDate from,
            LocalDate to
    ) {
        return habitRecordJpaRepository
                .findCompletionsForOwnerBetweenDates(
                        owner.type(),
                        owner.ownerId(),
                        from,
                        to
                )
                .stream()
                .collect(Collectors.groupingBy(HabitRecordEntity::getCompletedDate))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new HabitGrassAggregation(
                                entry.getValue().size(),
                                entry.getValue()
                                        .stream()
                                        .mapToInt(this::durationMinutesOf)
                                        .sum()
                        )
                ));
    }

    private int durationMinutesOf(HabitRecordEntity entity) {
        return entity.getDurationMinutes() != null ? entity.getDurationMinutes() : 0;
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
