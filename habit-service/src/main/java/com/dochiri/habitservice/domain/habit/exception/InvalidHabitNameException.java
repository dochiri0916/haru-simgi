package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitNameException extends DomainException {

    public InvalidHabitNameException(String value) {
        super(HabitErrorCode.INVALID_HABIT_NAME, "value", value);
    }

}