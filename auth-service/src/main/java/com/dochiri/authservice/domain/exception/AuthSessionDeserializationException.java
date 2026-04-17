package com.dochiri.authservice.domain.exception;

import com.dochiri.errorhandling.BaseException;

public class AuthSessionDeserializationException extends BaseException {

    public AuthSessionDeserializationException(Throwable cause) {
        super(AuthErrorCode.AUTH_SESSION_DESERIALIZATION_FAILED, cause);
    }
}
