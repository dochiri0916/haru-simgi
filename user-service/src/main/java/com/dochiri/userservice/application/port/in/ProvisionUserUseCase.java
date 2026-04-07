package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.ProvisionUserCommand;
import com.dochiri.userservice.application.port.in.dto.ProvisionUserResult;

public interface ProvisionUserUseCase {

    ProvisionUserResult provision(ProvisionUserCommand command);

}
