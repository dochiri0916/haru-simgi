package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;

public class HabitAccessDeniedException extends DomainException {

    public HabitAccessDeniedException(HabitId habitId, HabitOwner actualOwner, HabitOwner requestOwner) {
        super(HabitErrorCode.HABIT_ACCESS_DENIED,
                "habitId", habitId.value(),
                "actualOwner", actualOwner.referenceId(),
                "requestOwner", requestOwner.referenceId());
    }

}