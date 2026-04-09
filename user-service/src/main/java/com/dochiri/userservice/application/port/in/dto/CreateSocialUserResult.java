package com.dochiri.userservice.application.port.in.dto;

public record CreateSocialUserResult(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
}
