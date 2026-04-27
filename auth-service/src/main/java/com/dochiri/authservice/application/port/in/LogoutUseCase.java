package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.LogoutCommand;

public interface LogoutUseCase {

    void execute(LogoutCommand command);
}
