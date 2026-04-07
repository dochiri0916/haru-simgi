package com.dochiri.userservice.application.port.in.dto;

public record ProvisionSocialUserCommand(
        String email,
        String nickname,
        String profileImageUrl
) {
}
