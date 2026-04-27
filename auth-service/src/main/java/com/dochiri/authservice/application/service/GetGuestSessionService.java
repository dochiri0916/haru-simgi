package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.GetGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.dto.GetGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.GetGuestSessionResult;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.infrastructure.configuration.GuestSessionProperties;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class GetGuestSessionService implements GetGuestSessionUseCase {

    private final GuestSessionRepository guestSessionRepository;
    private final GuestSessionTokenPort guestSessionTokenPort;
    private final GuestSessionProperties guestSessionProperties;
    private final Clock clock;

    @Transactional
    @Override
    public GetGuestSessionResult execute(GetGuestSessionCommand command) {
        Instant now = clock.instant();
        GuestSession session = guestSessionRepository.findByTokenHash(guestSessionTokenPort.hash(command.token()))
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_GUEST_SESSION));

        if (!session.isActiveAt(now)) {
            throw new BaseException(AuthErrorCode.INVALID_GUEST_SESSION);
        }

        GuestSession touched = guestSessionRepository.save(
                session.touch(now, now.plus(guestSessionProperties.expiration()))
        );

        return new GetGuestSessionResult(
                touched.publicId(),
                touched.status(),
                touched.expiresAt()
        );
    }
}
