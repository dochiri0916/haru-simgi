package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;

public interface AuthTokenIssueUseCase {

    IssueAuthTokenResult execute(IssueAuthTokenCommand command);

}
