package com.dochiri.habitservice.application.port.out;

import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;

import java.util.List;
import java.util.Optional;

public interface HabitRepository {

    Habit save(Habit habit);

    Optional<Habit> findById(String id);

    List<Habit> findByOwner(HabitOwner owner);

    void delete(String id);

}