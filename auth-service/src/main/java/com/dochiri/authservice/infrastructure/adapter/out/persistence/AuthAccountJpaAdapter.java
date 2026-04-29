package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.out.AuthAccountRepository;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
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
    public AuthAccount save(AuthAccount authAccount) {
        try {
            return persist(authAccount);
        } catch (DataIntegrityViolationException exception) {
            return retryUpdateAfterConcurrentInsert(authAccount, exception);
        }
    }

    @Override
    public Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId) {
        return authAccountJpaRepository.findByProviderAndProviderId(provider.name(), providerId)
                .map(authAccountMapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByPublicId(String publicId) {
        return authAccountJpaRepository.findByPublicId(publicId)
                .map(authAccountMapper::toDomain);
    }

    private AuthAccount persist(AuthAccount authAccount) {
        AuthAccountEntity entity = authAccountJpaRepository
                .findByProviderAndProviderId(authAccount.provider().name(), authAccount.providerId())
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
        return authAccountJpaRepository
                .findByProviderAndProviderId(authAccount.provider().name(), authAccount.providerId())
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
