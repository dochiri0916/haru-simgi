package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.KakaoAuthorizeUseCase;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthorizeService implements KakaoAuthorizeUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;

    @Override
    public String buildAuthorizeUrl(String state) {
        return kakaoOAuthPort.buildAuthorizeUrl(state);
    }

}
