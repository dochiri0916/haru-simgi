package com.dochiri.userservice.application.error;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    AUTH_ACCOUNT_SYNC_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "인증 서비스와 통신할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

}
