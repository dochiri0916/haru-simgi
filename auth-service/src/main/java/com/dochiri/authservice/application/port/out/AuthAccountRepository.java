package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.AuthAccount;

import java.util.Optional;

public interface AuthAccountRepository {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findByProviderAndProviderUserId(String provider, String providerUserId);

    Optional<AuthAccount> findByUserId(Long userId);
}
