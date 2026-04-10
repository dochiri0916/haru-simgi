package com.dochiri.habitservice.infrastructure.adapter.in.web;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.habitservice.application.error.HabitErrorCode;
import com.dochiri.habitservice.domain.exception.HabitAccessDeniedException;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import com.dochiri.habitservice.domain.exception.InvalidHabitIdException;
import com.dochiri.habitservice.domain.exception.InvalidHabitNameException;
import com.dochiri.habitservice.domain.exception.InvalidHabitOwnerException;
import com.dochiri.habitservice.domain.exception.InvalidHabitRecordIdException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class HabitExceptionHandler {

    @ExceptionHandler(HabitNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleHabitNotFound(HabitNotFoundException e) {
        return toResponse(HabitErrorCode.HABIT_NOT_FOUND, e);
    }

    @ExceptionHandler(HabitAccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleHabitAccessDenied(HabitAccessDeniedException e) {
        return toResponse(HabitErrorCode.HABIT_NOT_FOUND, e);
    }

    @ExceptionHandler(InvalidHabitIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidHabitId(InvalidHabitIdException e) {
        return toResponse(HabitErrorCode.INVALID_HABIT_ID, e);
    }

    @ExceptionHandler(InvalidHabitNameException.class)
    public ResponseEntity<ProblemDetail> handleInvalidHabitName(InvalidHabitNameException e) {
        return toResponse(HabitErrorCode.INVALID_HABIT_NAME, e);
    }

    @ExceptionHandler(InvalidHabitRecordIdException.class)
    public ResponseEntity<ProblemDetail> handleInvalidHabitRecordId(InvalidHabitRecordIdException e) {
        return toResponse(HabitErrorCode.INVALID_HABIT_RECORD_ID, e);
    }

    @ExceptionHandler(InvalidHabitOwnerException.class)
    public ResponseEntity<ProblemDetail> handleInvalidHabitOwner(InvalidHabitOwnerException e) {
        HabitErrorCode errorCode = e.getReason() == InvalidHabitOwnerException.Reason.INVALID_TYPE
                ? HabitErrorCode.INVALID_HABIT_OWNER_TYPE
                : HabitErrorCode.INVALID_HABIT_OWNER_REFERENCE_ID;
        return toResponse(errorCode, e);
    }

    private ResponseEntity<ProblemDetail> toResponse(HabitErrorCode errorCode, Exception cause) {
        BaseException baseException = new BaseException(errorCode, cause);
        return ResponseEntity.status(baseException.getStatusCode()).body(baseException.getBody());
    }

}
