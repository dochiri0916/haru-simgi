package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;
import com.dochiri.userservice.domain.UserId;

import java.util.Optional;

public interface UserRepository {

    Long save(User user);

    User loadByUserId(Long userId);

    User loadByPublicId(String publicId);

}