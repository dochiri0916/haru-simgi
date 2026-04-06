package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(String publicId);

    User loadById(String publicId);

    boolean existsByEmail(String email);
}
