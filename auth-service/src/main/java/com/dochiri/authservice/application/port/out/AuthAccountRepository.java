package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.errorhandling.BaseException;

import java.util.Optional;

public interface AuthAccountRepository {

    AuthAccount upsertByUserId(AuthAccount authAccount);

    default AuthAccount loadByEmail(String email) {
        return findByEmail(email)
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_CREDENTIALS));
    }

    Optional<AuthAccount> findByEmail(String email);

    Optional<AuthAccount> findByUserId(Long userId);
}
