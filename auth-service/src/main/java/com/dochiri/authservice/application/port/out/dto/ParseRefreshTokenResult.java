package com.dochiri.authservice.application.port.out.dto;

public record ParseRefreshTokenResult(
        Long userId,
        String tokenId
) {
}