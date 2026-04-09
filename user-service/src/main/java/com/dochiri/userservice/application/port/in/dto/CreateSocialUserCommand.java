package com.dochiri.userservice.application.port.in.dto;

public record CreateSocialUserCommand(
        String nickname,
        String profileImageUrl
) {
}
