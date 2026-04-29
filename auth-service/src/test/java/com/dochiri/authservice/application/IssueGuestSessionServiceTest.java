package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.IssueGuestSessionCommand;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.application.port.out.dto.GeneratedGuestSessionToken;
import com.dochiri.authservice.application.service.IssueGuestSessionService;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.authservice.domain.GuestSessionStatus;
import com.dochiri.authservice.infrastructure.configuration.GuestSessionProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class IssueGuestSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-04-27T00:00:00Z");

    private final GuestSessionRepository guestSessionRepository = mock(GuestSessionRepository.class);
    private final GuestSessionTokenPort guestSessionTokenPort = mock(GuestSessionTokenPort.class);
    private final GuestSessionProperties guestSessionProperties = new GuestSessionProperties(90);
    private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));
    private final IssueGuestSessionService issueGuestSessionService = new IssueGuestSessionService(
            guestSessionRepository,
            guestSessionTokenPort,
            guestSessionProperties,
            clock
    );

    @Test
    void 게스트_세션을_발급하고_토큰_해시만_저장한다() {
        when(guestSessionTokenPort.generate())
                .thenReturn(new GeneratedGuestSessionToken("raw-token", "token-hash"));
        when(guestSessionRepository.save(any(GuestSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = issueGuestSessionService.execute(new IssueGuestSessionCommand());

        ArgumentCaptor<GuestSession> sessionCaptor = ArgumentCaptor.forClass(GuestSession.class);
        verify(guestSessionRepository).save(sessionCaptor.capture());
        GuestSession savedSession = sessionCaptor.getValue();

        assertThat(savedSession.publicId()).isNotBlank();
        assertThat(savedSession.tokenHash()).isEqualTo("token-hash");
        assertThat(savedSession.tokenHash()).isNotEqualTo("raw-token");
        assertThat(savedSession.status()).isEqualTo(GuestSessionStatus.ACTIVE);
        assertThat(savedSession.createdAt()).isEqualTo(NOW);
        assertThat(savedSession.expiresAt()).isEqualTo(Instant.parse("2026-07-26T00:00:00Z"));

        assertThat(result.guestId()).isEqualTo(savedSession.publicId());
        assertThat(result.token()).isEqualTo("raw-token");
        assertThat(result.expiresAt()).isEqualTo(savedSession.expiresAt());
    }
}
