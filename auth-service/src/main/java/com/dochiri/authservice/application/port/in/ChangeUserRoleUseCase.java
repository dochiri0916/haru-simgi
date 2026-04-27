package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.ChangeUserRoleCommand;

public interface ChangeUserRoleUseCase {

    void execute(ChangeUserRoleCommand command);

}
