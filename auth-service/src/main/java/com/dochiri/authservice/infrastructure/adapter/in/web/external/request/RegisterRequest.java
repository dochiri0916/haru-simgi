package com.dochiri.authservice.infrastructure.adapter.in.web.external.request;

import com.dochiri.authservice.application.port.in.dto.RegisterCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password
) {
    public RegisterCommand toCommand() {
        return new RegisterCommand(email, password);
    }
}
