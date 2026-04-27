package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.GetCurrentUserCommand;
import com.dochiri.userservice.application.port.in.dto.GetCurrentUserResult;

public interface GetCurrentUserUseCase {

    GetCurrentUserResult execute(GetCurrentUserCommand command);

}
