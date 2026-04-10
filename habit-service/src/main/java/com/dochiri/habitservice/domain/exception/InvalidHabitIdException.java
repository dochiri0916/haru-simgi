package com.dochiri.habitservice.domain.exception;

public class InvalidHabitIdException extends HabitDomainException {

    public InvalidHabitIdException() {
        super("유효하지 않은 습관 ID입니다.");
    }

}
