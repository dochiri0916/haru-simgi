package com.dochiri.authservice.application.port.in.dto;

import org.springframework.util.Assert;

public record RegisterCommand(
        String email,
        String password
) {
    public RegisterCommand {
        Assert.hasText(email, "email must not be blank");
        Assert.hasText(password, "password must not be blank");
    }
}
