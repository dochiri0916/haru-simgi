package com.dochiri.userservice.application.port.in.dto;

public record CreateSocialUserResult(
        Long userId,
        String nickname,
        String profileImageUrl
) {
}
