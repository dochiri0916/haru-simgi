package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitIndex;
import com.dochiri.habitservice.domain.habit.HabitOwner;

import java.util.List;

public interface HabitRepository {

    Habit save(Habit habit);

    Habit loadById(HabitId id);

    List<Habit> findByOwner(HabitOwner owner);

    HabitIndex nextIndex(HabitOwner owner);

    int migrateOwner(HabitOwner sourceOwner, HabitOwner targetOwner);

    void delete(HabitId id);

}
