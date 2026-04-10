package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
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
        HabitEntity entity = HabitMapper.toEntity(habit);
        HabitEntity saved = habitJpaRepository.save(entity);
        return HabitMapper.toDomain(saved);
    }

    @Override
    public Optional<Habit> findById(String id) {
        return habitJpaRepository.findByPublicId(id)
                .map(HabitMapper::toDomain);
    }

    @Override
    public List<Habit> findByOwner(HabitOwner owner) {
        return habitJpaRepository.findByOwnerTypeAndOwnerReferenceId(owner.type().name(), owner.referenceId())
                .stream()
                .map(HabitMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(String id) {
        habitJpaRepository.findByPublicId(id)
                .ifPresent(habitJpaRepository::delete);
    }

}