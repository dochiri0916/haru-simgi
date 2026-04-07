package com.dochiri.authservice.application.port.out.dto;

public record ProvisionedUser(
        Long userId,
        String email
) {
}
