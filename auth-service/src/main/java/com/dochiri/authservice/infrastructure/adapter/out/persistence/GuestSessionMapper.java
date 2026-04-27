package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.GuestSession;
import org.springframework.stereotype.Component;

@Component
public class GuestSessionMapper {

    public GuestSessionEntity toEntity(GuestSession guestSession) {
        return new GuestSessionEntity(
                guestSession.publicId(),
                guestSession.tokenHash(),
                guestSession.status(),
                guestSession.createdAt(),
                guestSession.lastSeenAt(),
                guestSession.expiresAt(),
                guestSession.linkedUserPublicId(),
                guestSession.linkedAt()
        );
    }

    public GuestSession toDomain(GuestSessionEntity entity) {
        return new GuestSession(
                entity.getPublicId(),
                entity.getTokenHash(),
                entity.getStatus(),
                entity.getIssuedAt(),
                entity.getLastSeenAt(),
                entity.getExpiresAt(),
                entity.getLinkedUserPublicId(),
                entity.getLinkedAt()
        );
    }

    public void apply(GuestSession guestSession, GuestSessionEntity entity) {
        entity.apply(
                guestSession.status(),
                guestSession.lastSeenAt(),
                guestSession.expiresAt(),
                guestSession.linkedUserPublicId(),
                guestSession.linkedAt()
        );
    }
}
