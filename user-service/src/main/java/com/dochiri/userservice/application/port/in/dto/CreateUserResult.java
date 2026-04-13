package com.dochiri.userservice.application.port.in.dto;

public record CreateUserResult(
        Long userId,
        String publicId,
        String nickname,
        String profileImageUrl
) {
}