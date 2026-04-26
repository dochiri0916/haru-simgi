package com.dochiri.userservice.domain.exception;

import com.dochiri.errorhandling.DomainException;

public class InvalidProfileImageUrlException extends DomainException {

    public InvalidProfileImageUrlException(String value) {
        super(UserErrorCode.INVALID_PROFILE_IMAGE_URL, "value", value);
    }

}
