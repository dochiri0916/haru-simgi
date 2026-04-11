package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitRecordIdException extends DomainException {

    public InvalidHabitRecordIdException(String value) {
        super(HabitRecordErrorCode.INVALID_HABIT_RECORD_ID, "value", value);
    }

}