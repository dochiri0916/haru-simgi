package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.ProvisionUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ProvisionUserRequest(
        @Email
        @NotBlank
        String email
) {
    public ProvisionUserCommand toCommand() {
        return new ProvisionUserCommand(email);
    }
}
