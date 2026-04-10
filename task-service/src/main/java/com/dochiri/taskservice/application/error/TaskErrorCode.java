package com.dochiri.taskservice.application.error;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode implements ErrorCode {

    // 404 - NOT FOUND
    TASK_NOT_FOUND(NOT_FOUND, "해당 할 일을 찾을 수 없습니다."),
    TASK_OWNER_MISSING(BAD_REQUEST, "인증 사용자 또는 guestId 중 하나가 필요합니다."),

    // 409 - CONFLICT
    TASK_ALREADY_COMPLETED(CONFLICT, "이미 완료된 할 일입니다."),
    TASK_NOT_COMPLETED(CONFLICT, "완료되지 않은 할 일입니다."),

    // 403 - FORBIDDEN
    TASK_OWNER_FORBIDDEN(FORBIDDEN, "본인 소유의 할 일만 처리할 수 있습니다."),

    // 400 - VALIDATION
    TASK_OWNER_TYPE_MISSING(BAD_REQUEST, "할 일 소유자 타입은 필수입니다."),
    TASK_OWNER_REFERENCE_ID_MISSING(BAD_REQUEST, "할 일 소유자 식별자는 필수입니다."),
    TASK_OWNER_REFERENCE_ID_BLANK(BAD_REQUEST, "할 일 소유자 식별자는 비어 있을 수 없습니다."),
    TASK_TITLE_BLANK(BAD_REQUEST, "할 일 제목은 비어 있을 수 없습니다."),
    TASK_TITLE_TOO_LONG(BAD_REQUEST, "할 일 제목은 100자를 초과할 수 없습니다."),
    TASK_COMPLETED_AT_REQUIRED(BAD_REQUEST, "완료된 할 일에는 completedAt이 필요합니다."),
    TASK_COMPLETED_AT_MUST_BE_NULL(BAD_REQUEST, "미완료 할 일의 completedAt은 null이어야 합니다."),
    TASK_DUE_DATE_IN_PAST(BAD_REQUEST, "할 일 날짜는 오늘 이전으로 지정할 수 없습니다."),
    TASK_GRASS_INVALID_DATE_RANGE(BAD_REQUEST, "잔디 조회 시작일은 종료일보다 늦을 수 없습니다."),
    TASK_MIGRATION_SOURCE_NOT_GUEST(BAD_REQUEST, "비회원 할 일만 회원 계정으로 이관할 수 있습니다."),
    TASK_MIGRATION_TARGET_NOT_USER(BAD_REQUEST, "할 일 이관 대상은 회원 계정이어야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;

}