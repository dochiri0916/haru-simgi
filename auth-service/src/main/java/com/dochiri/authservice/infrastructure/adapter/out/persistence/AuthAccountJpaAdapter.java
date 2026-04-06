package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.infrastructure.AuthAccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountRepository {

    private final AuthAccountJpaRepository authAccountJpaRepository;
    private final AuthAccountMapper authAccountMapper;

    @Override
    public AuthAccount save(AuthAccount authAccount) {
        Optional<AuthAccountEntity> existing = authAccountJpaRepository.findByUserId(authAccount.userId());

        if (existing.isEmpty()) {
            return authAccountMapper.toDomain(authAccountJpaRepository.save(authAccountMapper.toEntity(authAccount)));
        }

        AuthAccountEntity entity = existing.get();
        authAccountMapper.apply(authAccount, entity);
        return authAccountMapper.toDomain(authAccountJpaRepository.save(entity));
    }

    @Override
    public Optional<AuthAccount> findByEmail(String email) {
        return authAccountJpaRepository.findByEmail(email)
                .map(authAccountMapper::toDomain);
    }
}
