package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.habit.HabitId;

public class HabitNotFoundException extends DomainException {

    public HabitNotFoundException(HabitId habitId) {
        super(HabitErrorCode.HABIT_NOT_FOUND, "id", habitId.value());
    }

}