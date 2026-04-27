package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.GuestSession;

import java.util.Optional;

public interface GuestSessionRepository {

    GuestSession save(GuestSession guestSession);

    Optional<GuestSession> findByTokenHash(String tokenHash);

}
