package com.dochiri.userservice.application.port.in.dto;

public record RegisterUserResult(
        String publicId,
        String email
) {
}