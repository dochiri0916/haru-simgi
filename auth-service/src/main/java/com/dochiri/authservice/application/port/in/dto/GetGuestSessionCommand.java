package com.dochiri.authservice.application.port.in.dto;

public record GetGuestSessionCommand(
        String token
) {
}
