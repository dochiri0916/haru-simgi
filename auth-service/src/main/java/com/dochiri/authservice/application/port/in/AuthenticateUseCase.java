package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;

public interface AuthenticateUseCase {

    AuthTokenResult authenticate(LoginCommand command);

}