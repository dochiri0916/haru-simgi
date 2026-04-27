package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.AuthSession;

import java.util.Optional;

public interface AuthSessionRepository {

    AuthSession saveReplacingUserSessions(AuthSession authSession);

    Optional<AuthSession> findByRefreshTokenId(String refreshTokenId);

    void deleteBySessionId(String sessionId);

    void deleteByPublicId(String publicId);
}
