package com.dochiri.userservice.application.port.in.dto;

public record CreateUserResult(
        Long userId,
        String email
) {
}
