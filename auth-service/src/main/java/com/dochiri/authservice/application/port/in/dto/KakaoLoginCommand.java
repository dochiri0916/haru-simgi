package com.dochiri.authservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record KakaoLoginCommand(
        String code,
        String guestSessionToken
) {
    public KakaoLoginCommand {
        requireNonNull(code);
    }
}
