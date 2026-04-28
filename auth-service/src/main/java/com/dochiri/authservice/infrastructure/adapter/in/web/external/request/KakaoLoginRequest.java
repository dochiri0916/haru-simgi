package com.dochiri.authservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import jakarta.validation.constraints.NotBlank;

public record KakaoLoginRequest(
        @NotBlank String code
) {
    public KakaoLoginCommand toCommand() {
        return new KakaoLoginCommand(code);
    }

    public KakaoLoginCommand toCommand(String guestSessionToken) {
        return new KakaoLoginCommand(code, guestSessionToken);
    }
}
