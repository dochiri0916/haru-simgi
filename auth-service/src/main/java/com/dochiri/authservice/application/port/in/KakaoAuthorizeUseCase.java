package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.KakaoAuthorizeCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoAuthorizeResult;

public interface KakaoAuthorizeUseCase {

    KakaoAuthorizeResult execute(KakaoAuthorizeCommand command);

}
