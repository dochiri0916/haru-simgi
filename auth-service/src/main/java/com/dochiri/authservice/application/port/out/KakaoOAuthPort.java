package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.KakaoUserProfile;

public interface KakaoOAuthPort {

    String buildAuthorizeUrl(String state);

    KakaoUserProfile authenticate(String authorizationCode);

}
