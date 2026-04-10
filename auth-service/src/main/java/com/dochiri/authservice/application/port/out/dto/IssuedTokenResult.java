package com.dochiri.authservice.application.port.out.dto;

import java.time.Instant;

public record IssuedTokenResult(
        String accessToken,
        String refreshToken,
        String refreshTokenId,
        Instant refreshTokenExpiresAt
) {
}
