package com.dochiri.authservice.domain.exception;

import com.dochiri.errorhandling.BaseException;

public class AuthSessionSerializationException extends BaseException {

    public AuthSessionSerializationException(Throwable cause) {
        super(AuthErrorCode.AUTH_SESSION_SERIALIZATION_FAILED, cause);
    }
}
