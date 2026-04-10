package com.dochiri.habitservice.application.error;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HabitErrorCode implements ErrorCode {

    // 400 - BAD_REQUEST
    INVALID_HABIT_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 ID입니다."),
    INVALID_HABIT_NAME(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 이름입니다."),
    INVALID_HABIT_RECORD_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 기록 ID입니다."),
    INVALID_CATEGORY_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 카테고리입니다."),
    INVALID_HABIT_OWNER_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 소유자 타입입니다."),
    INVALID_HABIT_OWNER_REFERENCE_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 소유자 식별자입니다."),

    // 404 - NOT_FOUND
    HABIT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 습관을 찾을 수 없습니다.")


    ;


    private final HttpStatus httpStatus;
    private final String message;

}