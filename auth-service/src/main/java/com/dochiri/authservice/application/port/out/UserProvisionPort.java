package com.dochiri.authservice.application.port.out;

import com.dochiri.authservice.application.port.out.dto.ProvisionedUser;

public interface UserProvisionPort {

    ProvisionedUser provision(String email);

}
