package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.VerifyGuestSessionResult;

public interface VerifyGuestSessionUseCase {

    VerifyGuestSessionResult execute(VerifyGuestSessionCommand command);

}
