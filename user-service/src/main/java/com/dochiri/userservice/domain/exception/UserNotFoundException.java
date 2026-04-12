package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.DomainException;
import com.dochiri.userservice.domain.UserId;

public class UserNotFoundException extends DomainException {

    public UserNotFoundException(UserId id) {
        super(UserErrorCode.USER_NOT_FOUND, "userId", id.value());
    }

    public UserNotFoundException(Long userId) {
        super(UserErrorCode.USER_NOT_FOUND, "userId", userId);
    }

}
