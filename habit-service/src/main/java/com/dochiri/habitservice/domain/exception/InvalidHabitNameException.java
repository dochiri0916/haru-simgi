package com.dochiri.habitservice.domain.exception;

public class InvalidHabitNameException extends HabitDomainException {

    public InvalidHabitNameException() {
        super("유효하지 않은 습관 이름입니다.");
    }

}
