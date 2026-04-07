package com.dochiri.userservice.infrastructure.adapter.in.web.internal.response;

import com.dochiri.userservice.application.port.in.dto.ProvisionUserResult;

public record ProvisionUserResponse(
        Long userId,
        String email
) {
    public static ProvisionUserResponse from(ProvisionUserResult result) {
        return new ProvisionUserResponse(result.userId(), result.email());
    }
}
