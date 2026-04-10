package com.dochiri.habitservice.domain.exception;

public class HabitNotFoundException extends HabitDomainException {

    public HabitNotFoundException() {
        super("해당 습관을 찾을 수 없습니다.");
    }

}
