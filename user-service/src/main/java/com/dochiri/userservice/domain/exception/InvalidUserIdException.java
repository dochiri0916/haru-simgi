package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidUserIdException extends DomainException {

    public InvalidUserIdException(String value) {
        super(UserErrorCode.INVALID_USER_ID, "value", value);
    }

}