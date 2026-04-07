package com.dochiri.authservice.infrastructure.adapter.out.http.response;

import com.dochiri.authservice.application.port.out.dto.ProvisionedUser;

public record ProvisionUserResponse(
        Long userId,
        String email
) {
    public ProvisionedUser toResult() {
        return new ProvisionedUser(userId, email);
    }
}
