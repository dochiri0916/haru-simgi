package com.dochiri.habitservice.domain.record.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidCompletedAtException extends DomainException {

    public InvalidCompletedAtException() {
        super(HabitRecordErrorCode.INVALID_COMPLETED_AT, "reason", "NULL_COMPLETED_AT");
    }

}
