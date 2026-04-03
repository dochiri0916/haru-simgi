package com.dochiri.userservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        String password
) {
    public RegisterUserCommand toCommand() {
        return new RegisterUserCommand(email, password);
    }
}
