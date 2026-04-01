package com.dochiri.authservice.infrastructure.repository;

import com.dochiri.authservice.infrastructure.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenId(String tokenId);

    void deleteByUserId(Long userId);
}
