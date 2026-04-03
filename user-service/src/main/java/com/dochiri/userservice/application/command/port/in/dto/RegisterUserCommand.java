package com.dochiri.userservice.application.command.port.in.dto;

import static java.util.Objects.requireNonNull;

public record RegisterUserCommand(
        String email,
        String password
) {
    public RegisterUserCommand {
        requireNonNull(email);
        requireNonNull(password);
    }
}