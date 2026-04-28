package com.dochiri.authservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.authservice.application.port.in.dto.GuestMergeStatus;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginResult;
import com.dochiri.security.role.UserRole;

import java.time.Instant;

public record AuthTokenResponse(
        String tokenType,
        String accessToken,
        String refreshToken,
        Instant refreshTokenExpiresAt,
        UserRole role,
        GuestMergeStatus guestMerge
) {
    public static AuthTokenResponse from(IssueAuthTokenResult result) {
        return new AuthTokenResponse(
                "Bearer",
                result.accessToken(),
                result.refreshToken(),
                result.refreshTokenExpiresAt(),
                result.role(),
                null
        );
    }

    public static AuthTokenResponse from(KakaoLoginResult result) {
        return new AuthTokenResponse(
                "Bearer",
                result.tokens().accessToken(),
                result.tokens().refreshToken(),
                result.tokens().refreshTokenExpiresAt(),
                result.tokens().role(),
                result.guestMerge()
        );
    }
}
