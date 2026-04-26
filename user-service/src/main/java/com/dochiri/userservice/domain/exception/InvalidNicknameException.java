package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidNicknameException extends DomainException {

    public InvalidNicknameException(String value) {
        super(UserErrorCode.INVALID_NICKNAME, "value", value);
    }

}
