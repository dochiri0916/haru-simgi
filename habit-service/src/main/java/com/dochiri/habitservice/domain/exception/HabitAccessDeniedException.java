package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class HabitAccessDeniedException extends DomainException {

    public HabitAccessDeniedException() {
        super("해당 습관에 접근할 수 없습니다.");
    }

}