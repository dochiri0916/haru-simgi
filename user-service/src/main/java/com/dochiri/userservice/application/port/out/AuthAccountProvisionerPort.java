package com.dochiri.userservice.application.port.out;

import com.dochiri.security.role.UserRole;

public interface AuthAccountProvisionerPort {

    void provision(Long userId, String publicId, String email, String passwordHash, UserRole role);

}
