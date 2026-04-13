package com.dochiri.authservice.infrastructure.adapter.out.cache;

import com.dochiri.authservice.application.port.out.RefreshTokenRepository;
import com.dochiri.authservice.domain.RefreshToken;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@ConditionalOnProperty(name = "spring.data.redis.host", matchIfMissing = false)
public class RefreshTokenRedisAdapter implements RefreshTokenRepository {

    private final StringRedisTemplate redisTemplate;

    // Key prefixes
    private static final String KEY_BY_USER_ID = "rt:uid:";
    private static final String KEY_BY_TOKEN_ID = "rt:tid:";

    public RefreshTokenRedisAdapter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public RefreshToken replaceByUserId(RefreshToken refreshToken) {
        String userIdKey = KEY_BY_USER_ID + refreshToken.getUserId();
        String tokenIdKey = KEY_BY_TOKEN_ID + refreshToken.getTokenId();

        // 기존 tokenId 조회 (있으면 삭제)
        String existingTokenId = redisTemplate.opsForValue().get(userIdKey);
        if (existingTokenId != null) {
            redisTemplate.delete(KEY_BY_TOKEN_ID + existingTokenId);
        }

        // 새로운 토큰 저장
        long ttlMillis = refreshToken.getExpiresAt().toEpochMilli() - System.currentTimeMillis();
        long ttlSeconds = Math.max(ttlMillis / 1000, 1); // 최소 1초

        redisTemplate.opsForValue().set(userIdKey, refreshToken.getTokenId(), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.opsForValue().set(tokenIdKey, String.valueOf(refreshToken.getUserId()), ttlSeconds, TimeUnit.SECONDS);

        return refreshToken;
    }

    @Override
    public Optional<RefreshToken> findByTokenId(String tokenId) {
        String userId = redisTemplate.opsForValue().get(KEY_BY_TOKEN_ID + tokenId);

        if (userId == null) {
            return Optional.empty();
        }

        // TTL 조회하여 expiresAt 계산
        Long ttlSeconds = redisTemplate.getExpire(KEY_BY_TOKEN_ID + tokenId, TimeUnit.SECONDS);
        if (ttlSeconds == null || ttlSeconds < 0) {
            return Optional.empty();
        }

        Instant expiresAt = Instant.now().plusSeconds(ttlSeconds);

        return Optional.of(RefreshToken.create(
                tokenId,
                Long.parseLong(userId),
                expiresAt
        ));
    }

    @Override
    public void deleteByUserId(Long userId) {
        String userIdKey = KEY_BY_USER_ID + userId;

        // tokenId 조회 후 두 키 모두 삭제
        String tokenId = redisTemplate.opsForValue().get(userIdKey);
        if (tokenId != null) {
            redisTemplate.delete(KEY_BY_TOKEN_ID + tokenId);
        }

        redisTemplate.delete(userIdKey);
    }
}
