package com.dochiri.habitservice.domain.exception;

public class HabitAccessDeniedException extends HabitDomainException {

    public HabitAccessDeniedException() {
        super("해당 습관에 접근할 수 없습니다.");
    }

}
