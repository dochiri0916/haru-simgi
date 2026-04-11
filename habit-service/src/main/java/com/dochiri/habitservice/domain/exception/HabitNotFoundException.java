package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitId;
import lombok.Getter;

@Getter
public class HabitNotFoundException extends DomainException {

    private final HabitId habitId;

    public HabitNotFoundException(HabitId habitId) {
        super(HabitErrorCode.HABIT_NOT_FOUND);
        this.habitId = habitId;
    }

}