package com.dochiri.userservice.application.port.in.dto;

public record GetCurrentUserResult(
        String id,
        String nickname,
        String profileImageUrl
) {
}