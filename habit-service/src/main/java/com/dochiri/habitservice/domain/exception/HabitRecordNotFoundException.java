package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitRecordId;
import lombok.Getter;

@Getter
public class HabitRecordNotFoundException extends DomainException {

    private final HabitRecordId habitRecordId;

    public HabitRecordNotFoundException(HabitRecordId id) {
        super(HabitRecordErrorCode.HABIT_RECORD_NOT_FOUND);
        this.habitRecordId = id;
    }

}