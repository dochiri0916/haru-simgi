package com.dochiri.userservice.application.port.in;

import com.dochiri.userservice.domain.User;

public interface GetCurrentUserUseCase {

    User getCurrentUser(Long userId);
}
