package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import lombok.Getter;

@Getter
public class InvalidHabitIdException extends DomainException {

    private final String value;

    public InvalidHabitIdException(String value) {
        super(HabitErrorCode.INVALID_HABIT_ID);
        this.value = value;
    }

}