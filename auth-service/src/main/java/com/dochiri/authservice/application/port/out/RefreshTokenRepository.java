package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken replaceByUserId(RefreshToken refreshToken);

    Optional<RefreshToken> findByTokenId(String tokenId);

    void deleteByUserId(Long userId);
}
