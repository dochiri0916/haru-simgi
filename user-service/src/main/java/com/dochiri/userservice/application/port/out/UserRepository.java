package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;

import java.util.Optional;

public interface UserRepository {

    Long create(User user);

    Optional<User> findByPublicId(String publicId);

    User loadByPublicId(String publicId);

    User loadByUserId(Long userId);

}
