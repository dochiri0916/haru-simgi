package com.dochiri.authservice.application.port.in.dto;

public record LoginCommand(
        String email,
        String password
) {
}
