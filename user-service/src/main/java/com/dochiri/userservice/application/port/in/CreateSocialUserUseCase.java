package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.application.port.in.dto.CreateSocialUserCommand;
import com.dochiri.userservice.application.port.in.dto.CreateSocialUserResult;

public interface CreateSocialUserUseCase {

    CreateSocialUserResult create(CreateSocialUserCommand command);

}
