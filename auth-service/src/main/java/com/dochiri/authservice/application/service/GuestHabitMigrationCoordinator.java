package com.dochiri.authservice.application.service;

import com.dochiri.authservice.application.port.in.dto.GuestMergeStatus;
import com.dochiri.authservice.application.port.out.GuestHabitMigrationPort;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.domain.GuestSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GuestHabitMigrationCoordinator {

    private static final Logger log = LoggerFactory.getLogger(GuestHabitMigrationCoordinator.class);

    private final GuestSessionRepository guestSessionRepository;
    private final GuestSessionTokenPort guestSessionTokenPort;
    private final GuestHabitMigrationPort guestHabitMigrationPort;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GuestMergeStatus migrate(String guestSessionToken, String userPublicId) {
        if (!StringUtils.hasText(guestSessionToken)) {
            return GuestMergeStatus.SKIPPED;
        }

        Instant now = clock.instant();
        Optional<GuestSession> activeSession = guestSessionRepository
                .findByTokenHash(guestSessionTokenPort.hash(guestSessionToken))
                .filter(session -> session.isActiveAt(now));
        if (activeSession.isEmpty()) {
            return GuestMergeStatus.SKIPPED;
        }

        GuestSession session = activeSession.get();
        try {
            guestHabitMigrationPort.migrate(session.publicId(), userPublicId);
            guestSessionRepository.save(session.linkToUser(userPublicId, now));
            return GuestMergeStatus.SUCCEEDED;
        } catch (RuntimeException exception) {
            log.warn("게스트 습관 이전 실패: guestId={}, userPublicId={}", session.publicId(), userPublicId, exception);
            return GuestMergeStatus.FAILED;
        }
    }
}
