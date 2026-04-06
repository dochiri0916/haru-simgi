package com.dochiri.authservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
public record RefreshTokenRequest(
        String refreshToken
) {
    public RefreshTokenCommand toCommand() {
        return new RefreshTokenCommand(refreshToken);
    }
}
