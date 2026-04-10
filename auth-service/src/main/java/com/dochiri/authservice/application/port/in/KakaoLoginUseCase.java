package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;

public interface KakaoLoginUseCase {

    String buildAuthorizeUrl(String state);

    IssueAuthTokenResult login(KakaoLoginCommand command);

}
