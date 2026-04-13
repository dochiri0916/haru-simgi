package com.dochiri.authservice.application.port.out.dto;

public record ParseRefreshTokenResult(
        String publicId,
        String tokenId
) {
}