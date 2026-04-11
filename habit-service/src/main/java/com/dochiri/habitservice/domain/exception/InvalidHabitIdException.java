package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import lombok.Getter;

@Getter
public class InvalidHabitRecordIdException extends DomainException {

    private final String value;

    public InvalidHabitRecordIdException(String value) {
        super(HabitRecordErrorCode.INVALID_HABIT_RECORD_ID);
        this.value = value;
    }

}