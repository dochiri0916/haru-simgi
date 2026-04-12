package com.dochiri.authservice.infrastructure.adapter.in.web.dev;

import com.dochiri.authservice.application.port.in.AuthTokenIssueUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenCommand;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.AuthTokenCookieManager;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.AuthTokenResponse;
import com.dochiri.security.role.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevTokenController {

    static final String AUTH_TRANSPORT_HEADER = "X-Auth-Transport";

    private final AuthTokenIssueUseCase authTokenIssueUseCase;
    private final AuthTokenCookieManager authTokenCookieManager;

    @PostMapping("/token")
    public ResponseEntity<AuthTokenResponse> issueToken(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "USER") String role,
            HttpServletRequest httpServletRequest
    ) {
        var result = authTokenIssueUseCase.issue(new IssueAuthTokenCommand(userId, UserRole.from(role)));

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        applyAuthCookiesIfNeeded(responseBuilder, httpServletRequest, authTokenCookieManager.createAuthCookieHeaders(result));
        return responseBuilder.body(AuthTokenResponse.from(result));
    }

    private void applyAuthCookiesIfNeeded(
            ResponseEntity.HeadersBuilder<?> responseBuilder,
            HttpServletRequest request,
            List<String> cookieHeaders
    ) {
        if (usesCookies(request)) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, cookieHeaders.toArray(String[]::new));
        }
    }

    private boolean usesCookies(HttpServletRequest request) {
        String transport = request.getHeader(AUTH_TRANSPORT_HEADER);
        return !"bearer".equalsIgnoreCase(transport);
    }
}
