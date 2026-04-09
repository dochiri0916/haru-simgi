package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateUserResult;

public interface CreateUserUseCase {

    CreateUserResult create(CreateUserCommand command);

}
