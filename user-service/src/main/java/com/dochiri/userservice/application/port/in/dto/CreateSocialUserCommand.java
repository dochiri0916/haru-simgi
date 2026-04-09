package com.dochiri.userservice.application.port.in.dto;

public record CreateSocialUserCommand(
        String email,
        String nickname,
        String profileImageUrl
) {
}
