package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;

import java.util.Optional;

public interface AuthAccountRepository {

    AuthAccount save(AuthAccount authAccount);

    Optional<AuthAccount> findByProviderAndProviderId(AuthProvider provider, String providerId);

    Optional<AuthAccount> findByPublicId(String publicId);
}
