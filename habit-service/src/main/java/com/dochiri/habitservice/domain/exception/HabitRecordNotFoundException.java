package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitRecordId;

public class HabitRecordNotFoundException extends DomainException {

    public HabitRecordNotFoundException(HabitRecordId id) {
        super(HabitRecordErrorCode.HABIT_RECORD_NOT_FOUND, "habitRecordId", id.value());
    }

}