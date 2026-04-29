package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.IssueGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.IssueGuestSessionResult;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.authservice.infrastructure.configuration.GuestSessionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IssueGuestSessionService implements IssueGuestSessionUseCase {

    private final GuestSessionRepository guestSessionRepository;
    private final GuestSessionTokenPort guestSessionTokenPort;
    private final GuestSessionProperties guestSessionProperties;
    private final Clock clock;

    @Transactional
    @Override
    public IssueGuestSessionResult execute(IssueGuestSessionCommand command) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(guestSessionProperties.expiration());
        var generatedToken = guestSessionTokenPort.generate();

        GuestSession guestSession = GuestSession.create(
                UUID.randomUUID().toString(),
                generatedToken.tokenHash(),
                now,
                expiresAt
        );
        GuestSession saved = guestSessionRepository.save(guestSession);

        return new IssueGuestSessionResult(
                saved.publicId(),
                generatedToken.token(),
                saved.expiresAt()
        );
    }
}
