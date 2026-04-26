package com.dochiri.authservice.infrastructure.adapter.out.cache.exception;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.errorhandling.BaseException;

public class AuthSessionSerializationException extends BaseException {

    public AuthSessionSerializationException(Throwable cause) {
        super(AuthErrorCode.AUTH_SESSION_SERIALIZATION_FAILED, cause);
    }
}
