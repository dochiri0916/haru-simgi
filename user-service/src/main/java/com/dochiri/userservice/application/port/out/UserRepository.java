package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.domain.User;

public interface UserRepository {

    Long save(User user);

    User loadById(String id);

}