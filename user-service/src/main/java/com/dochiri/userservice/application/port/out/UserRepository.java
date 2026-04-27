package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user, String idempotencyKey);

    Optional<User> findByIdempotencyKey(String idempotencyKey);

    User loadById(String id);

}
