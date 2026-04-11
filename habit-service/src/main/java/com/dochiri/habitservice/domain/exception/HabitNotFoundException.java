package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitId;

public class HabitNotFoundException extends DomainException {

    public HabitNotFoundException(HabitId habitId) {
        super(HabitErrorCode.HABIT_NOT_FOUND, "habitId", habitId.value());
    }

}