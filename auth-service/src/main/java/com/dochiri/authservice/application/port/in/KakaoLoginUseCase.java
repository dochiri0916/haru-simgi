package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginResult;

public interface KakaoLoginUseCase {

    KakaoLoginResult execute(KakaoLoginCommand command);

}
