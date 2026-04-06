package com.dochiri.authservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
public record LogoutRequest(
        String refreshToken
) {
    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }
}
