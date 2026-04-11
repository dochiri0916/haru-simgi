package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitOwnerException extends DomainException {

    public enum Reason { INVALID_TYPE, INVALID_REFERENCE_ID }

    private final Reason reason;

    public InvalidHabitOwnerException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }

}
