package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.in.dto.RegisterUserResult;

public interface RegisterUserUseCase {

    RegisterUserResult register(RegisterUserCommand command);

}
