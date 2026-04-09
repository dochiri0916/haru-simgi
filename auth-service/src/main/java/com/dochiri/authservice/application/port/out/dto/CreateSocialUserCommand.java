package com.dochiri.authservice.application.port.out.dto;

public record CreateSocialUserCommand(
        String email,
        String nickname,
        String profileImageUrl
) {
}
