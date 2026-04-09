package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.CreateUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateUserResult;

public interface UserCreatePort {

    CreateUserResult create(CreateUserCommand command);

}
