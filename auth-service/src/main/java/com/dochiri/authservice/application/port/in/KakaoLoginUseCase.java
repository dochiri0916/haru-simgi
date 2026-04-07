package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;

public interface KakaoLoginUseCase {

    String buildAuthorizeUrl(String state);

    AuthTokenResult login(KakaoLoginCommand command);

}
