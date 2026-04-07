package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RegisterCommand;

public interface RegisterUseCase {

    AuthTokenResult register(RegisterCommand command);

}
