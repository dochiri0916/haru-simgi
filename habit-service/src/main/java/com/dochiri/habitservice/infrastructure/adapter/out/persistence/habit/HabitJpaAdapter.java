package com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit;

import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.habit.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HabitJpaAdapter implements HabitRepository {

    private final HabitJpaRepository habitJpaRepository;

    @Override
    public Habit save(Habit habit) {
        HabitEntity entity = habitJpaRepository.findByPublicId(habit.getId().value())
                .map(existing -> {
                    HabitMapper.updateEntity(habit, existing);
                    return existing;
                })
                .orElseGet(() -> HabitMapper.toEntity(habit));

        HabitEntity saved = habitJpaRepository.save(entity);
        return HabitMapper.toDomain(saved);
    }

    @Override
    public Optional<Habit> findById(HabitId id) {
        return habitJpaRepository.findByPublicId(id.value())
                .map(HabitMapper::toDomain);
    }

    @Override
    public Habit loadById(HabitId id) {
        return findById(id)
                .orElseThrow(() -> new HabitNotFoundException(id));
    }

    @Override
    public List<Habit> findByOwner(HabitOwner owner) {
        return habitJpaRepository.findByOwnerTypeAndOwnerPublicIdOrderByIndexAscCreatedAtAsc(owner.type(), owner.ownerId())
                .stream()
                .map(HabitMapper::toDomain)
                .toList();
    }

    @Override
    public HabitIndex nextIndex(HabitOwner owner) {
        return habitJpaRepository.findTopByOwnerTypeAndOwnerPublicIdOrderByIndexDesc(owner.type(), owner.ownerId())
                .map(HabitEntity::getIndex)
                .map(maxIndex -> HabitIndex.of(maxIndex + 1))
                .orElseGet(() -> HabitIndex.of(0));
    }

    @Override
    public void delete(HabitId id) {
        HabitEntity entity = habitJpaRepository.findByPublicId(id.value())
                .orElseThrow(() -> new HabitNotFoundException(id));

        habitJpaRepository.delete(entity);
    }

}
