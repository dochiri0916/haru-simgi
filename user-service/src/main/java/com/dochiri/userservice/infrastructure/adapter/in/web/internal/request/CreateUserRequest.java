package com.dochiri.userservice.infrastructure.adapter.in.web.internal.request;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @Email
        @NotBlank
        String email
) {
    public CreateUserCommand toCommand() {
        return new CreateUserCommand(email);
    }
}
