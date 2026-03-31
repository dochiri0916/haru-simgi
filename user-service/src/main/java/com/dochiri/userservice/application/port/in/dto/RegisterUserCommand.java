package com.dochiri.userservice.application.port.in.dto;

public record RegisterUserCommand(
        String email,
        String password
) {
}
