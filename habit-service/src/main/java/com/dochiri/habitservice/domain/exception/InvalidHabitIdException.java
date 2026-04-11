package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitIdException extends DomainException {

    public InvalidHabitIdException() {
        super("유효하지 않은 습관 ID입니다.");
    }

}
