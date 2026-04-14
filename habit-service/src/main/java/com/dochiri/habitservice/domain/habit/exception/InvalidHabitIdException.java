package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitIdException extends DomainException {

    public InvalidHabitIdException(String value) {
        super(HabitErrorCode.INVALID_HABIT_ID, "value", value);
    }

}