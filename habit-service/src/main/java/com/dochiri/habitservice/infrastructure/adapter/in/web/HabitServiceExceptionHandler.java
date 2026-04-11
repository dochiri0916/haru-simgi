package com.dochiri.habitservice.infrastructure.adapter.in.web;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.habitservice.application.error.HabitErrorCode;
import com.dochiri.habitservice.domain.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Habit 서비스의 도메인 예외를 HTTP 응답으로 변환
 * GlobalExceptionHandler보다 우선 실행되어 구체적인 예외를 처리
 */
@Slf4j
@RestControllerAdvice("com.dochiri.habitservice")
@Order(-1)
public class HabitServiceExceptionHandler {

    @ExceptionHandler({
        HabitNotFoundException.class,
        HabitAccessDeniedException.class,
        InvalidHabitIdException.class,
        InvalidHabitNameException.class,
        InvalidHabitRecordIdException.class,
        InvalidHabitOwnerException.class
    })
    public ResponseEntity<Object> handleHabitDomainException(Exception exception) {
        HabitErrorCode errorCode = mapToErrorCode(exception);
        BaseException baseException = new BaseException(errorCode, exception);

        log.warn("Habit domain exception: code={}, message={}, exception={}",
                errorCode.name(),
                exception.getMessage(),
                exception.getClass().getSimpleName());

        return ResponseEntity
                .status(baseException.getStatusCode())
                .body(baseException.getBody());
    }

    private HabitErrorCode mapToErrorCode(Exception exception) {
        return switch (exception) {
            case HabitNotFoundException e -> HabitErrorCode.HABIT_NOT_FOUND;
            case HabitAccessDeniedException e -> HabitErrorCode.HABIT_NOT_FOUND;
            case InvalidHabitIdException e -> HabitErrorCode.INVALID_HABIT_ID;
            case InvalidHabitNameException e -> HabitErrorCode.INVALID_HABIT_NAME;
            case InvalidHabitRecordIdException e -> HabitErrorCode.INVALID_HABIT_RECORD_ID;
            case InvalidHabitOwnerException e ->
                e.getReason() == InvalidHabitOwnerException.Reason.INVALID_TYPE
                    ? HabitErrorCode.INVALID_HABIT_OWNER_TYPE
                    : HabitErrorCode.INVALID_HABIT_OWNER_REFERENCE_ID;
            default -> throw new IllegalStateException("Unmapped exception: " + exception.getClass().getSimpleName());
        };
    }

}
