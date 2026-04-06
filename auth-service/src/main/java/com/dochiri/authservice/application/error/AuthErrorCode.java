package com.dochiri.authservice.application.error;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTH_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 인증 계정을 찾을 수 없습니다."),
    AUTH_ACCOUNT_CONFLICT(HttpStatus.CONFLICT, "인증 계정 동기화 중 충돌이 발생했습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "사용자 서비스와 통신할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
