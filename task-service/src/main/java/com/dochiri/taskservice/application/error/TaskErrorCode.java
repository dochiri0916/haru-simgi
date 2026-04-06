package com.dochiri.taskservice.application.error;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TaskErrorCode implements ErrorCode {

    TASK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 할 일을 찾을 수 없습니다."),
    TASK_TITLE_BLANK(HttpStatus.BAD_REQUEST, "할 일 제목은 비어 있을 수 없습니다."),
    TASK_TITLE_TOO_LONG(HttpStatus.BAD_REQUEST, "할 일 제목은 100자를 초과할 수 없습니다."),
    TASK_MIGRATION_SOURCE_NOT_GUEST(HttpStatus.BAD_REQUEST, "비회원 할 일만 회원 계정으로 이관할 수 있습니다."),
    TASK_MIGRATION_TARGET_NOT_USER(HttpStatus.BAD_REQUEST, "할 일 이관 대상은 회원 계정이어야 합니다.");

    private final HttpStatus httpStatus;
    private final String message;

}