package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidHabitRecordIdException extends DomainException {

    public InvalidHabitRecordIdException() {
        super("유효하지 않은 습관 기록 ID입니다.");
    }

}
