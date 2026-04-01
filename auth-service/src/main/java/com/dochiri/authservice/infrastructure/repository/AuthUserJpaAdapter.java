package com.dochiri.authservice.infrastructure.repository;

import com.dochiri.authservice.application.port.out.AuthUserRepository;
import com.dochiri.authservice.domain.AuthUser;
import com.dochiri.authservice.infrastructure.AuthUserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthUserJpaAdapter implements AuthUserRepository {

    private final AuthUserJpaRepository authUserJpaRepository;
    private final AuthUserMapper authUserMapper;

    @Override
    public AuthUser save(AuthUser authUser) {
        Optional<AuthUserEntity> existing = authUserJpaRepository.findByUserId(authUser.userId());

        if (existing.isEmpty()) {
            return authUserMapper.toDomain(authUserJpaRepository.save(authUserMapper.toEntity(authUser)));
        }

        AuthUserEntity entity = existing.get();
        authUserMapper.apply(authUser, entity);
        return authUserMapper.toDomain(entity);
    }

    @Override
    public Optional<AuthUser> findByEmail(String email) {
        return authUserJpaRepository.findByEmail(email)
                .map(authUserMapper::toDomain);
    }
}
