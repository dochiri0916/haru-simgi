package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 사용자 ID입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String message;

}