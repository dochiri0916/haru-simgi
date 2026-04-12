package com.dochiri.authservice.application.port.in;

public interface KakaoAuthorizeUseCase {

    String buildAuthorizeUrl(String state);

}
