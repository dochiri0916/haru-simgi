package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.Id;
import com.dochiri.userservice.domain.User;

import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Id id);

    User loadById(Id id);

    boolean existsByEmail(String email);
}
