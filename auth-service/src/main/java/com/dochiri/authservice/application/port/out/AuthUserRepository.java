package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.domain.AuthUser;

import java.util.Optional;

public interface AuthUserRepository {

    AuthUser save(AuthUser authUser);

    Optional<AuthUser> findByEmail(String email);
}
