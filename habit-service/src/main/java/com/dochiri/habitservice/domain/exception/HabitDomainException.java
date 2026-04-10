package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public abstract class HabitDomainException extends DomainException {

    protected HabitDomainException(String message) {
        super(message);
    }

}
