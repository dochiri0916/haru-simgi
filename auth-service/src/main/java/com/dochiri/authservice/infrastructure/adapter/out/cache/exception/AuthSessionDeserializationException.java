package com.dochiri.authservice.infrastructure.adapter.out.cache.exception;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.errorhandling.BaseException;

public class AuthSessionDeserializationException extends BaseException {

    public AuthSessionDeserializationException(Throwable cause) {
        super(AuthErrorCode.AUTH_SESSION_DESERIALIZATION_FAILED, cause);
    }
}
