package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.ProvisionSocialUserCommand;
import com.dochiri.userservice.application.port.in.dto.ProvisionSocialUserResult;

public interface ProvisionSocialUserUseCase {

    ProvisionSocialUserResult provision(ProvisionSocialUserCommand command);

}
