package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;

public interface SocialUserCreatePort {

    CreateSocialUserResult create(CreateSocialUserCommand command);

}
