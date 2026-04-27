package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.application.port.out.GuestSessionRepository;
import com.dochiri.authservice.domain.GuestSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GuestSessionJpaAdapter implements GuestSessionRepository {

    private final GuestSessionJpaRepository guestSessionJpaRepository;
    private final GuestSessionMapper guestSessionMapper;

    @Override
    public GuestSession save(GuestSession guestSession) {
        GuestSessionEntity entity = guestSessionJpaRepository.findByPublicId(guestSession.publicId())
                .map(existing -> {
                    guestSessionMapper.apply(guestSession, existing);
                    return existing;
                })
                .orElseGet(() -> guestSessionMapper.toEntity(guestSession));

        return guestSessionMapper.toDomain(guestSessionJpaRepository.saveAndFlush(entity));
    }

    @Override
    public Optional<GuestSession> findByTokenHash(String tokenHash) {
        return guestSessionJpaRepository.findByTokenHash(tokenHash)
                .map(guestSessionMapper::toDomain);
    }
}
