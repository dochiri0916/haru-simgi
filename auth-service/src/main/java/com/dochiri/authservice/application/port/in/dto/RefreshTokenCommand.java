package com.dochiri.authservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record RefreshTokenCommand(
        String refreshToken
) {
    public RefreshTokenCommand {
        requireNonNull(refreshToken);
    }
}