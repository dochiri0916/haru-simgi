package com.dochiri.authservice.infrastructure.adapter.in.web.dev;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.AuthTokenResponse;
import com.dochiri.security.role.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final AuthTokenIssueUseCase authTokenIssueUseCase;

    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> issueToken(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "USER") String role
    ) {
        return ResponseEntity.ok(AuthTokenResponse.from(
                authTokenIssueUseCase.issue(new IssueAuthTokenCommand(userId, UserRole.from(role)))
        ));
    }
}
