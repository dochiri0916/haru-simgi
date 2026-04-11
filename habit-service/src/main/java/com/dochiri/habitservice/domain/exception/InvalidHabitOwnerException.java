package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.OwnerType;

public class InvalidHabitOwnerException extends DomainException {

    public enum Reason {
        INVALID_TYPE,
        INVALID_REFERENCE_ID
    }

    public InvalidHabitOwnerException(Reason reason, OwnerType type, String referenceId) {
        super(HabitErrorCode.INVALID_HABIT_OWNER,
                "reason", reason,
                "type", type,
                "referenceId", referenceId);
    }

}