package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.dto.GetGuestSessionCommand;
import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.application.port.out.GuestSessionTokenPort;
import com.dochiri.authservice.application.service.GetGuestSessionService;
import com.dochiri.authservice.domain.GuestSession;
import com.dochiri.authservice.domain.GuestSessionStatus;
import com.dochiri.authservice.infrastructure.configuration.GuestSessionProperties;
import com.dochiri.errorhandling.BaseException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class GetGuestSessionServiceTest {

    private static final Instant NOW = Instant.parse("2026-04-27T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-07-26T00:00:00Z");

    private final GuestSessionRepository guestSessionRepository = mock(GuestSessionRepository.class);
    private final GuestSessionTokenPort guestSessionTokenPort = mock(GuestSessionTokenPort.class);
    private final GuestSessionProperties guestSessionProperties = new GuestSessionProperties(90);
    private final Clock clock = Clock.fixed(NOW, ZoneId.of("UTC"));
    private final GetGuestSessionService getGuestSessionService = new GetGuestSessionService(
            guestSessionRepository,
            guestSessionTokenPort,
            guestSessionProperties,
            clock
    );

    @Test
    void 활성_게스트_세션을_조회하고_만료시각을_연장한다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW.minusSeconds(60), EXPIRES_AT);
        when(guestSessionTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(guestSessionRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(session));
        when(guestSessionRepository.save(any(GuestSession.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = getGuestSessionService.execute(new GetGuestSessionCommand("raw-token"));

        ArgumentCaptor<GuestSession> sessionCaptor = ArgumentCaptor.forClass(GuestSession.class);
        verify(guestSessionRepository).save(sessionCaptor.capture());
        GuestSession touched = sessionCaptor.getValue();

        assertThat(touched.lastSeenAt()).isEqualTo(NOW);
        assertThat(touched.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(result.guestId()).isEqualTo("guest-id");
        assertThat(result.status()).isEqualTo(GuestSessionStatus.ACTIVE);
        assertThat(result.expiresAt()).isEqualTo(EXPIRES_AT);
    }

    @Test
    void 토큰에_해당하는_세션이_없으면_예외가_발생한다() {
        when(guestSessionTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(guestSessionRepository.findByTokenHash("token-hash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getGuestSessionService.execute(new GetGuestSessionCommand("raw-token")))
                .isInstanceOf(BaseException.class);

        verify(guestSessionRepository, never()).save(any());
    }

    @Test
    void 만료된_세션이면_예외가_발생한다() {
        GuestSession session = GuestSession.create(
                "guest-id",
                "token-hash",
                Instant.parse("2026-01-01T00:00:00Z"),
                NOW
        );
        when(guestSessionTokenPort.hash("raw-token")).thenReturn("token-hash");
        when(guestSessionRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> getGuestSessionService.execute(new GetGuestSessionCommand("raw-token")))
                .isInstanceOf(BaseException.class);

        verify(guestSessionRepository, never()).save(any());
    }
}
