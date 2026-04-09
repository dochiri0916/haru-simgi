package com.dochiri.userservice.application.port.in.dto;

import org.springframework.util.Assert;

public record CreateUserCommand(
        String email
) {
    public CreateUserCommand {
        Assert.hasText(email, "email must not be blank");
    }
}
