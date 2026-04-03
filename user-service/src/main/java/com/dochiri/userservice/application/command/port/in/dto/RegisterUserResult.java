package com.dochiri.userservice.application.command.port.in.dto;

public record RegisterUserResult(
        String publicId,
        String email
) {
}