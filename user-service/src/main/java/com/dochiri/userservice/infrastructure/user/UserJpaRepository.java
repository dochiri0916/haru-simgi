package com.dochiri.userservice.infrastructure.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByPublicId(String publicId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

}
