package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;
import lombok.Getter;

@Getter
public class HabitAccessDeniedException extends DomainException {

    private final HabitId habitId;
    private final HabitOwner actualOwner;
    private final HabitOwner requestOwner;

    public HabitAccessDeniedException(
            HabitId habitId,
            HabitOwner actualOwner,
            HabitOwner requestOwner
    ) {
        super(HabitErrorCode.HABIT_ACCESS_DENIED);
        this.habitId = habitId;
        this.actualOwner = actualOwner;
        this.requestOwner = requestOwner;
    }

}