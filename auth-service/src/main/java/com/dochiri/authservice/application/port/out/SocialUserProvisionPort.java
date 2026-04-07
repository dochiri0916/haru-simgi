package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.ProvisionedSocialUser;

public interface SocialUserProvisionPort {

    ProvisionedSocialUser provision(String email, String nickname, String profileImageUrl);

}
