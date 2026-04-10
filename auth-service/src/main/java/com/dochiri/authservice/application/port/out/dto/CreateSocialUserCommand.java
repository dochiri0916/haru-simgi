package com.dochiri.authservice.application.port.out.dto;

public record CreateSocialUserCommand(
        String nickname,
        String profileImageUrl
) {
}