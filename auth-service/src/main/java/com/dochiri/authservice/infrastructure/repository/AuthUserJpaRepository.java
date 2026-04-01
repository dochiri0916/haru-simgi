package com.dochiri.authservice.infrastructure.repository;

import com.dochiri.authservice.infrastructure.AuthUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserJpaRepository extends JpaRepository<AuthUserEntity, Long> {

    Optional<AuthUserEntity> findByEmail(String email);

    Optional<AuthUserEntity> findByUserId(Long userId);
}
