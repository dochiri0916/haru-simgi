package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.VerifyGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionResult;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
@RequiredArgsConstructor
public class VerifyGuestSessionService implements VerifyGuestSessionUseCase {

    private final GuestSessionRepository guestSessionRepository;
    private final GuestSessionTokenPort guestSessionTokenPort;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public VerifyGuestSessionResult execute(VerifyGuestSessionCommand command) {
        GuestSession session = guestSessionRepository.findByTokenHash(guestSessionTokenPort.hash(command.token()))
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_GUEST_SESSION));

        if (!session.isActiveAt(clock.instant())) {
            throw new BaseException(AuthErrorCode.INVALID_GUEST_SESSION);
        }

        return new VerifyGuestSessionResult(
                session.publicId(),
                session.status(),
                session.expiresAt()
        );
    }
}
