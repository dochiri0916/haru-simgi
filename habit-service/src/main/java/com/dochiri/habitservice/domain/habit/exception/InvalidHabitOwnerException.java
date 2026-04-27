package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.habit.OwnerType;

public class InvalidHabitOwnerException extends DomainException {

    public enum Reason {
        INVALID_TYPE,
        INVALID_OWNER_ID
    }

    public InvalidHabitOwnerException(Reason reason, OwnerType type, String ownerId) {
        super(HabitErrorCode.INVALID_HABIT_OWNER,
                "reason", reason,
                "type", String.valueOf(type),
                "ownerId", String.valueOf(ownerId));
    }

}
