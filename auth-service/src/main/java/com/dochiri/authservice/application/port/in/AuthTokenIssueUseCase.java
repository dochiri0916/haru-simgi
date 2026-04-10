package com.dochiri.authservice.application.port.in;

import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;

public interface AuthTokenIssueUseCase {

    IssueAuthTokenResult issue(IssueAuthTokenCommand command);

}