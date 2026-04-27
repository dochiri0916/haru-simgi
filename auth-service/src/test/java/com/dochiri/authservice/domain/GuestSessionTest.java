package com.dochiri.authservice.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuestSessionTest {

    private static final Instant NOW = Instant.parse("2026-04-27T00:00:00Z");
    private static final Instant EXPIRES_AT = Instant.parse("2026-07-26T00:00:00Z");

    @Test
    void 게스트_세션을_생성하면_ACTIVE_상태가_된다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW, EXPIRES_AT);

        assertThat(session.publicId()).isEqualTo("guest-id");
        assertThat(session.tokenHash()).isEqualTo("token-hash");
        assertThat(session.status()).isEqualTo(GuestSessionStatus.ACTIVE);
        assertThat(session.createdAt()).isEqualTo(NOW);
        assertThat(session.lastSeenAt()).isEqualTo(NOW);
        assertThat(session.expiresAt()).isEqualTo(EXPIRES_AT);
        assertThat(session.linkedUserPublicId()).isNull();
        assertThat(session.linkedAt()).isNull();
    }

    @Test
    void ACTIVE이고_만료되지_않은_세션만_활성으로_본다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW, EXPIRES_AT);

        assertThat(session.isActiveAt(NOW)).isTrue();
        assertThat(session.isActiveAt(EXPIRES_AT)).isFalse();
    }

    @Test
    void touch하면_마지막_사용시각과_만료시각을_갱신한다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW, EXPIRES_AT);
        Instant touchedAt = Instant.parse("2026-04-28T00:00:00Z");
        Instant nextExpiresAt = Instant.parse("2026-07-27T00:00:00Z");

        GuestSession touched = session.touch(touchedAt, nextExpiresAt);

        assertThat(touched.createdAt()).isEqualTo(NOW);
        assertThat(touched.lastSeenAt()).isEqualTo(touchedAt);
        assertThat(touched.expiresAt()).isEqualTo(nextExpiresAt);
        assertThat(touched.status()).isEqualTo(GuestSessionStatus.ACTIVE);
    }

    @Test
    void 사용자와_연동하면_LINKED_상태가_되고_사용자_publicId를_남긴다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW, EXPIRES_AT);
        Instant linkedAt = Instant.parse("2026-04-28T00:00:00Z");

        GuestSession linked = session.linkToUser("user-public-id", linkedAt);

        assertThat(linked.status()).isEqualTo(GuestSessionStatus.LINKED);
        assertThat(linked.linkedUserPublicId()).isEqualTo("user-public-id");
        assertThat(linked.linkedAt()).isEqualTo(linkedAt);
        assertThat(linked.lastSeenAt()).isEqualTo(linkedAt);
        assertThat(linked.isActiveAt(linkedAt)).isFalse();
    }

    @Test
    void 만료된_세션은_touch하거나_연동할_수_없다() {
        GuestSession session = GuestSession.create("guest-id", "token-hash", NOW, EXPIRES_AT);
        Instant expiredAt = EXPIRES_AT;

        assertThatThrownBy(() -> session.touch(expiredAt, expiredAt.plusSeconds(60)))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> session.linkToUser("user-public-id", expiredAt))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 생성시_만료시각은_현재시각보다_뒤여야_한다() {
        assertThatThrownBy(() -> GuestSession.create("guest-id", "token-hash", NOW, NOW))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
