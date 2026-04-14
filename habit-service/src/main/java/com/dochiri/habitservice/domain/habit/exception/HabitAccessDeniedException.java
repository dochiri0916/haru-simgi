package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;

public class HabitAccessDeniedException extends DomainException {

    public HabitAccessDeniedException(HabitId habitId, HabitOwner actualOwner, HabitOwner requestOwner) {
        super(HabitErrorCode.HABIT_ACCESS_DENIED,
                "habitId", habitId.value(),
                "actualOwner", actualOwner.ownerId(),
                "requestOwner", requestOwner.ownerId());
    }

}
