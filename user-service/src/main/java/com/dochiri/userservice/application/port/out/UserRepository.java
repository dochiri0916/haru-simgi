package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;

import java.util.Optional;

public interface UserRepository {

    Long save(User user);

    Optional<User> findById(String id);

    User loadByUserId(Long userId);

}