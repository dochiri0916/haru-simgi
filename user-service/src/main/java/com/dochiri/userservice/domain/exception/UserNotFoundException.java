package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(String id) {
        super(UserErrorCode.USER_NOT_FOUND, "id", id);
    }

}
