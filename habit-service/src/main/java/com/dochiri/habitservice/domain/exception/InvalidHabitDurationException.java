package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitDurationException extends DomainException {

    public InvalidHabitDurationException(int minutes) {
        super(HabitRecordErrorCode.INVALID_HABIT_DURATION, "minutes", minutes);
    }

}