package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.KakaoAuthorizeUseCase;
import com.dochiri.authservice.application.port.in.dto.KakaoAuthorizeCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoAuthorizeResult;
import com.dochiri.authservice.application.port.out.KakaoOAuthPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KakaoAuthorizeService implements KakaoAuthorizeUseCase {

    private final KakaoOAuthPort kakaoOAuthPort;

    @Override
    public KakaoAuthorizeResult execute(KakaoAuthorizeCommand command) {
        return new KakaoAuthorizeResult(kakaoOAuthPort.buildAuthorizeUrl(command.state()));
    }

}
