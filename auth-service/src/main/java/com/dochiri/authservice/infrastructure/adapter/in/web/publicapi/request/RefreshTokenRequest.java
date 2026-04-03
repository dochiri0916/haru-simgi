package com.dochiri.authservice.infrastructure.adapter.in.web.publicapi.request;

import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
        @NotBlank
        String refreshToken
) {
    public RefreshTokenCommand toCommand() {
        return new RefreshTokenCommand(refreshToken);
    }
}
