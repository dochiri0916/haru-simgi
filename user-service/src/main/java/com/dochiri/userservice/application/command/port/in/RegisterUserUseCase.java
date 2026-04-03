package com.dochiri.userservice.application.command.port.in;

import com.dochiri.userservice.application.command.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.command.port.in.dto.RegisterUserResult;

public interface RegisterUserUseCase {

    RegisterUserResult register(RegisterUserCommand command);

}