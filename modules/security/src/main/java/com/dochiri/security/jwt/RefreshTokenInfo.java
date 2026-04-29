package com.dochiri.security.jwt;

public record RefreshTokenInfo(
        String publicId,
        String tokenId
) {
}
