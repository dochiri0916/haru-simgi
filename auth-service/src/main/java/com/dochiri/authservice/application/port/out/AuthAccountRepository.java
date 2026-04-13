package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.AuthAccount;

import java.util.Optional;

public interface AuthAccountRepository {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findByProviderAndProviderId(String provider, String providerId);

    Optional<AuthAccount> findByUserId(Long userId);

    Optional<AuthAccount> findByPublicId(String publicId);
}
