package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitIndexException extends DomainException {

    public InvalidHabitIndexException(int value) {
        super(HabitErrorCode.INVALID_HABIT_INDEX, "value", value);
    }

}
