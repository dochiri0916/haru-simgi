package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthAccountJpaRepository extends JpaRepository<AuthAccountEntity, Long> {

    Optional<AuthAccountEntity> findByEmail(String email);

    Optional<AuthAccountEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

}
