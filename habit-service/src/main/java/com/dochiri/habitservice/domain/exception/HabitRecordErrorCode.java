package com.dochiri.habitservice.domain.exception;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HabitRecordErrorCode implements ErrorCode {

    HABIT_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 습관 기록을 찾을 수 없습니다."),
    INVALID_HABIT_RECORD_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 습관 기록 ID입니다."),
    INVALID_HABIT_RECORD_VALUE(HttpStatus.BAD_REQUEST, "기록 값은 음수일 수 없습니다."),
    INVALID_COMPLETED_AT(HttpStatus.BAD_REQUEST, "완료 시간은 유효한 값이어야 합니다."),
    DUPLICATE_HABIT_RECORD(HttpStatus.CONFLICT, "이미 해당 시간에 기록이 존재합니다."),
    HABIT_RECORD_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 습관 기록에 접근할 수 없습니다."),
    INVALID_HABIT_DURATION(HttpStatus.BAD_REQUEST, "습관 수행 시간은 0 이상이어야 합니다.");

    private final HttpStatus status;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}