package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @Query("select u.id from UserEntity u where u.email = :email")
    Optional<Long> findIdByEmail(String email);

    Optional<UserEntity> findByPublicId(String publicId);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

}
