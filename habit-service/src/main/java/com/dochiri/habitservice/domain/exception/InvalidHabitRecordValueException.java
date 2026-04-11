package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import lombok.Getter;

@Getter
public class InvalidHabitRecordValueException extends DomainException {

    private final int value;

    public InvalidHabitRecordValueException(int value) {
        super(HabitRecordErrorCode.INVALID_HABIT_RECORD_VALUE);
        this.value = value;
    }

}