package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuestSessionJpaRepository extends JpaRepository<GuestSessionEntity, Long> {

    Optional<GuestSessionEntity> findByTokenHash(String tokenHash);

    Optional<GuestSessionEntity> findByPublicId(String publicId);

}
