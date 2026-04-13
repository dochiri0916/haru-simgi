package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.GetCurrentUserResult;

public interface GetCurrentUserUseCase {

    GetCurrentUserResult getCurrentUser(String publicId);

}