package com.dochiri.authservice.application.port.out.dto;

public record CreateUserResult(
        Long userId,
        String email
) {
}
