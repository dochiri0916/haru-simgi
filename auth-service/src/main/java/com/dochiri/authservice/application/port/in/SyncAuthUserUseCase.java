package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;

public interface SyncAuthUserUseCase {

    void sync(SyncAuthUserCommand command);
}
