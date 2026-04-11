package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;

import java.util.List;
import java.util.Optional;

public interface HabitRepository {

    Habit save(Habit habit);

    Optional<Habit> findById(HabitId id);

    Habit loadById(HabitId id);

    List<Habit> findByOwner(HabitOwner owner);

    void delete(HabitId id);

}