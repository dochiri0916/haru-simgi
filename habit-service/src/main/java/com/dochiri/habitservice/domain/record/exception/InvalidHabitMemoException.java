package com.dochiri.habitservice.domain.record.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitMemoException extends DomainException {

    public InvalidHabitMemoException(int length) {
        super(HabitRecordErrorCode.INVALID_HABIT_MEMO, "length", length);
    }

}
