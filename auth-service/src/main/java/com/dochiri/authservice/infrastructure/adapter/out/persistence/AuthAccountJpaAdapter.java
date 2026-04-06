package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.infrastructure.AuthAccountEntity;
import com.dochiri.errorhandling.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AuthAccountJpaAdapter implements AuthAccountRepository {

    private final AuthAccountJpaRepository authAccountJpaRepository;
    private final AuthAccountMapper authAccountMapper;

    @Override
    public AuthAccount upsertByUserId(AuthAccount authAccount) {
        try {
            return persist(authAccount);
        } catch (DataIntegrityViolationException exception) {
            return retryUpdateAfterConcurrentInsert(authAccount, exception);
        }
    }

    @Override
    public Optional<AuthAccount> findByEmail(String email) {
        return authAccountJpaRepository.findByEmail(email)
                .map(authAccountMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return authAccountJpaRepository.findByUserId(userId)
                .map(authAccountMapper::toDomain);
    }

    private AuthAccount persist(AuthAccount authAccount) {
        AuthAccountEntity entity = authAccountJpaRepository.findByUserId(authAccount.userId())
                .map(existing -> {
                    authAccountMapper.apply(authAccount, existing);
                    return existing;
                })
                .orElseGet(() -> authAccountMapper.toEntity(authAccount));

        AuthAccountEntity saved = authAccountJpaRepository.saveAndFlush(entity);
        return authAccountMapper.toDomain(saved);
    }

    private AuthAccount retryUpdateAfterConcurrentInsert(
            AuthAccount authAccount,
            DataIntegrityViolationException originalException
    ) {
        return authAccountJpaRepository.findByUserId(authAccount.userId())
                .map(existing -> {
                    authAccountMapper.apply(authAccount, existing);

                    try {
                        AuthAccountEntity saved = authAccountJpaRepository.saveAndFlush(existing);
                        return authAccountMapper.toDomain(saved);
                    } catch (DataIntegrityViolationException retryException) {
                        throw new BaseException(AuthErrorCode.AUTH_ACCOUNT_CONFLICT, retryException);
                    }
                })
                .orElseThrow(() -> new BaseException(AuthErrorCode.AUTH_ACCOUNT_CONFLICT, originalException));
    }

}