package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.RegisterUseCase;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.KakaoLoginRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.LoginRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.LogoutRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.RefreshTokenRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.RegisterRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.AuthTokenResponse;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.KakaoAuthorizeUrlResponse;
import com.dochiri.authservice.infrastructure.config.KakaoLoginProperties;
import com.dochiri.errorhandling.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final RegisterUseCase registerUseCase;
    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final AuthTokenCookieManager authTokenCookieManager;
    private final KakaoLoginProperties kakaoLoginProperties;

    @PostMapping("/register")
    public ResponseEntity<AuthTokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUseCase.register(request.toCommand());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .body(AuthTokenResponse.from(result));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthTokenResponse> login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUseCase.authenticate(request.toCommand());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .body(AuthTokenResponse.from(result));
    }

    @GetMapping("/login/kakao/authorize")
    public ResponseEntity<KakaoAuthorizeUrlResponse> kakaoAuthorizeUrl(
            @RequestParam(required = false) String state
    ) {
        return ResponseEntity.ok(new KakaoAuthorizeUrlResponse(kakaoLoginUseCase.buildAuthorizeUrl(state)));
    }

    @PostMapping("/login/kakao")
    public ResponseEntity<AuthTokenResponse> kakaoLogin(@Valid @RequestBody KakaoLoginRequest request) {
        var result = kakaoLoginUseCase.login(request.toCommand());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .body(AuthTokenResponse.from(result));
    }

    @GetMapping("/login/kakao/callback")
    public ResponseEntity<Void> kakaoLoginCallback(@RequestParam String code) {
        var result = kakaoLoginUseCase.login(new KakaoLoginCommand(code));
        return ResponseEntity.status(302)
                .location(URI.create(kakaoLoginProperties.frontendRedirectUri()))
                .header(HttpHeaders.SET_COOKIE, authTokenCookieManager.createAuthCookieHeaders(result).toArray(String[]::new))
                .build();
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
