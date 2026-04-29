package com.dochiri.authservice.infrastructure.adapter.in.web.internal.request;

import jakarta.validation.constraints.NotBlank;

public record VerifyGuestSessionRequest(
        @NotBlank
        String token
) {
}
