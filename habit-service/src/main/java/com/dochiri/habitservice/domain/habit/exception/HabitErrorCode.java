package com.dochiri.habitservice.domain.habit.exception;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HabitErrorCode implements ErrorCode {

    HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 습관을 찾을 수 없습니다."),
    INVALID_HABIT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 ID입니다."),
    INVALID_HABIT_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 이름입니다."),
    INVALID_HABIT_INDEX(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 순서입니다."),
    INVALID_HABIT_OWNER(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 소유자입니다."),
    HABIT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
