package com.dochiri.authservice.application.port.in.dto;

import static java.util.Objects.requireNonNull;

public record LogoutCommand(
        String refreshToken
) {
    public LogoutCommand {
        requireNonNull(refreshToken);
    }
}