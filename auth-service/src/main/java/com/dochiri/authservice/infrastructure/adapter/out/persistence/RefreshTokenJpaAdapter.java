package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.RefreshToken;
import com.dochiri.authservice.infrastructure.RefreshTokenEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenJpaAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public RefreshToken replaceByUserId(RefreshToken refreshToken) {
        RefreshTokenEntity saved = refreshTokenJpaRepository.findByUserId(refreshToken.getUserId())
                .map(existing -> {
                    existing.update(refreshToken.getTokenId(), refreshToken.getExpiresAt());
                    return refreshTokenJpaRepository.save(existing);
                })
                .orElseGet(() -> refreshTokenJpaRepository.save(
                        RefreshTokenEntity.from(
                                refreshToken.getUserId(),
                                refreshToken.getTokenId(),
                                refreshToken.getExpiresAt()
                        )
                ));

        return RefreshToken.create(saved.getTokenId(), saved.getUserId(), saved.getExpiresAt());
    }

    @Override
    public Optional<RefreshToken> findByTokenId(String tokenId) {
        return refreshTokenJpaRepository.findByTokenId(tokenId)
                .map(entity -> RefreshToken.create(entity.getTokenId(), entity.getUserId(), entity.getExpiresAt()));
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenJpaRepository.deleteByUserId(userId);
    }
}
