package com.dochiri.userservice.application.port.in.dto;

import org.springframework.util.Assert;

public record ProvisionUserCommand(
        String email
) {
    public ProvisionUserCommand {
        Assert.hasText(email, "email must not be blank");
    }
}
