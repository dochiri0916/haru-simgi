package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.util.List;
import java.util.Optional;

public interface HabitRepository {

    Habit save(Habit habit);

    Optional<Habit> findById(HabitId id);

    Habit loadById(HabitId id);

    List<Habit> findByOwner(HabitOwner owner);

    HabitIndex nextIndex(HabitOwner owner);

    void delete(HabitId id);

}
