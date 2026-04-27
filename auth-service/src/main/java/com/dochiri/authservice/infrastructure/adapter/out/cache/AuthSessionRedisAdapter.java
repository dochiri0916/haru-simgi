package com.dochiri.authservice.infrastructure.adapter.out.cache;

import com.dochiri.authservice.application.port.out.AuthSessionRepository;
import com.dochiri.authservice.domain.AuthSession;
import com.dochiri.authservice.infrastructure.adapter.out.cache.exception.AuthSessionDeserializationException;
import com.dochiri.authservice.infrastructure.adapter.out.cache.exception.AuthSessionSerializationException;
import com.dochiri.authservice.infrastructure.configuration.AuthSessionRedisKeyProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class AuthSessionRedisAdapter implements AuthSessionRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuthSessionRedisKeyProperties keyProperties;
    private final Clock clock;

    @Override
    public AuthSession saveReplacingUserSessions(AuthSession authSession) {
        deleteByPublicId(authSession.publicId());

        long ttlSeconds = ttlSeconds(authSession.expiresAt());
        redisTemplate.opsForValue().set(
                sessionKey(authSession.sessionId()),
                serialize(authSession),
                ttlSeconds,
                TimeUnit.SECONDS
        );
        redisTemplate.opsForValue().set(
                refreshTokenKey(authSession.sessionId()),
                authSession.sessionId(),
                ttlSeconds,
                TimeUnit.SECONDS
        );
        redisTemplate.opsForSet().add(userSessionsKey(authSession.publicId()), authSession.sessionId());
        redisTemplate.expire(userSessionsKey(authSession.publicId()), ttlSeconds, TimeUnit.SECONDS);

        return authSession;
    }

    @Override
    public Optional<AuthSession> findByRefreshTokenId(String refreshTokenId) {
        String sessionId = redisTemplate.opsForValue().get(refreshTokenKey(refreshTokenId));
        if (sessionId == null) {
            return Optional.empty();
        }

        String sessionJson = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (sessionJson == null) {
            redisTemplate.delete(refreshTokenKey(refreshTokenId));
            return Optional.empty();
        }

        AuthSession authSession = deserialize(sessionJson);
        if (authSession.expiresAt().isBefore(clock.instant())) {
            deleteBySessionId(authSession.sessionId());
            return Optional.empty();
        }

        return Optional.of(authSession);
    }

    @Override
    public void deleteBySessionId(String sessionId) {
        findSessionById(sessionId).ifPresent(authSession ->
                redisTemplate.opsForSet().remove(userSessionsKey(authSession.publicId()), sessionId)
        );
        redisTemplate.delete(sessionKey(sessionId));
        redisTemplate.delete(refreshTokenKey(sessionId));
    }

    @Override
    public void deleteByPublicId(String publicId) {
        String userSessionsKey = userSessionsKey(publicId);
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);

        List<String> keysToDelete = new ArrayList<>();
        keysToDelete.add(userSessionsKey);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                keysToDelete.add(sessionKey(sessionId));
                keysToDelete.add(refreshTokenKey(sessionId));
            }
        }
        redisTemplate.delete(keysToDelete);
    }

    private Optional<AuthSession> findSessionById(String sessionId) {
        String sessionJson = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (sessionJson == null) {
            return Optional.empty();
        }
        return Optional.of(deserialize(sessionJson));
    }

    private long ttlSeconds(Instant expiresAt) {
        return Math.max(Duration.between(clock.instant(), expiresAt).toSeconds(), 1);
    }

    private String serialize(AuthSession authSession) {
        try {
            return objectMapper.writeValueAsString(authSession);
        } catch (JsonProcessingException exception) {
            throw new AuthSessionSerializationException(exception);
        }
    }

    private AuthSession deserialize(String sessionJson) {
        try {
            return objectMapper.readValue(sessionJson, AuthSession.class);
        } catch (JsonProcessingException exception) {
            throw new AuthSessionDeserializationException(exception);
        }
    }

    private String sessionKey(String sessionId) {
        return keyProperties.session() + sessionId;
    }

    private String refreshTokenKey(String refreshTokenId) {
        return keyProperties.refresh() + refreshTokenId;
    }

    private String userSessionsKey(String publicId) {
        return keyProperties.userSessions() + publicId;
    }
}
