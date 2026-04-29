package com.dochiri.authservice.application.port.in.dto;

public record VerifyGuestSessionCommand(
        String token
) {
}
