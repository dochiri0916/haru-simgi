package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.GetGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.GetGuestSessionResult;

public interface GetGuestSessionUseCase {

    GetGuestSessionResult execute(GetGuestSessionCommand command);

}
