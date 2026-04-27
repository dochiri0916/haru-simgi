package com.dochiri.authservice.application.port.out.dto;

public record CreateSocialUserCommand(
        String idempotencyKey,
        String nickname,
        String profileImageUrl
) {
}
