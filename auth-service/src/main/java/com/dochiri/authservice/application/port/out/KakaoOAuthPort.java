package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.KakaoAuthenticationCommand;
import com.dochiri.authservice.application.port.out.dto.KakaoUserProfileResult;

public interface KakaoOAuthPort {

    String buildAuthorizeUrl(String state);

    KakaoUserProfileResult authenticate(KakaoAuthenticationCommand command);

}
