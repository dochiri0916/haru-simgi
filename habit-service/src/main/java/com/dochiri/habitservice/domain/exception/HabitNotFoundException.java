package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class HabitNotFoundException extends DomainException {

    public HabitNotFoundException() {
        super("해당 습관을 찾을 수 없습니다.");
    }

}
