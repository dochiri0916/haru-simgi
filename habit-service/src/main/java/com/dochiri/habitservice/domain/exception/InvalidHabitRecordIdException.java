package com.dochiri.habitservice.domain.exception;

public class InvalidHabitRecordIdException extends HabitDomainException {

    public InvalidHabitRecordIdException() {
        super("유효하지 않은 습관 기록 ID입니다.");
    }

}
