package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitColorException extends DomainException {

    public InvalidHabitColorException(String value) {
        super(HabitErrorCode.INVALID_HABIT_COLOR, "value", value);
    }

}