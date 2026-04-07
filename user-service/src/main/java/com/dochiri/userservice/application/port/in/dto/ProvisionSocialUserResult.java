package com.dochiri.userservice.application.port.in.dto;

public record ProvisionSocialUserResult(
        Long userId,
        String email,
        String nickname,
        String profileImageUrl
) {
}
