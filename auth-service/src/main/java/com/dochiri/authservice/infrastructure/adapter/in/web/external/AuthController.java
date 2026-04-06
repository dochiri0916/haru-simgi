package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.LoginRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.LogoutRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.RefreshTokenRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.AuthTokenResponse;
import com.dochiri.errorhandling.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthTokenCookieManager authTokenCookieManager;

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUseCase.authenticate(request.toCommand());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .body(AuthTokenResponse.from(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var result = reissueTokenUseCase.reissue(
                new RefreshTokenCommand(
                        authTokenCookieManager.resolveRefreshToken(
                                request != null ? request.refreshToken() : null,
                                httpServletRequest
                        )
                )
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .body(AuthTokenResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            logoutUseCase.logout(
                    new LogoutCommand(
                            authTokenCookieManager.resolveRefreshToken(
                                    request != null ? request.refreshToken() : null,
                                    httpServletRequest
                            )
                    )
            );
        } catch (BaseException exception) {
            if (!"INVALID_REFRESH_TOKEN".equals(exception.getErrorCode().name())) {
                throw exception;
            }
        }

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.clearAuthCookieHeaders().toArray(String[]::new))
                .build();
    }
}
