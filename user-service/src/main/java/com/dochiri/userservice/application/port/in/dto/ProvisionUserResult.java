package com.dochiri.userservice.application.port.in.dto;

public record ProvisionUserResult(
        Long userId,
        String email
) {
}
