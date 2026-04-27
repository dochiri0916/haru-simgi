package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;

public interface ReissueTokenUseCase {

    IssueAuthTokenResult execute(RefreshTokenCommand command);
}
