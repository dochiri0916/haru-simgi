package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.KakaoAuthorizeUseCase;
import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.IssueGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.GetGuestSessionUseCase;
import com.dochiri.authservice.application.port.in.dto.GetGuestSessionCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoAuthorizeCommand;
import com.dochiri.authservice.application.port.in.dto.KakaoLoginCommand;
import com.dochiri.authservice.application.port.in.dto.LogoutCommand;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.KakaoLoginRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.LogoutRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.request.RefreshTokenRequest;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.AuthTokenResponse;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.GuestSessionResponse;
import com.dochiri.authservice.infrastructure.adapter.in.web.external.response.KakaoAuthorizeUrlResponse;
import com.dochiri.authservice.infrastructure.configuration.KakaoLoginProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    static final String AUTH_TRANSPORT_HEADER = "X-Auth-Transport";

    private final KakaoAuthorizeUseCase kakaoAuthorizeUseCase;
    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final ReissueTokenUseCase reissueTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final IssueGuestSessionUseCase issueGuestSessionUseCase;
    private final GetGuestSessionUseCase getGuestSessionUseCase;
    private final AuthTokenCookieManager authTokenCookieManager;
    private final GuestSessionCookieManager guestSessionCookieManager;
    private final KakaoLoginProperties kakaoLoginProperties;

    @GetMapping("/login/kakao/authorize")
    public ResponseEntity<KakaoAuthorizeUrlResponse> kakaoAuthorizeUrl(
            @RequestParam(required = false) String state
    ) {
        var result = kakaoAuthorizeUseCase.execute(new KakaoAuthorizeCommand(state));
        return ResponseEntity.ok(new KakaoAuthorizeUrlResponse(result.authorizeUrl()));
    }

    @PostMapping("/guest")
    public ResponseEntity<GuestSessionResponse> issueGuestSession() {
        var result = issueGuestSessionUseCase.execute();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, guestSessionCookieManager.createGuestSessionCookieHeader(result))
                .body(GuestSessionResponse.from(result));
    }

    @GetMapping("/guest/me")
    public ResponseEntity<GuestSessionResponse> getGuestSession(HttpServletRequest httpServletRequest) {
        var result = getGuestSessionUseCase.execute(new GetGuestSessionCommand(
                guestSessionCookieManager.resolveGuestSessionToken(httpServletRequest)
        ));
        return ResponseEntity.ok()
                .body(GuestSessionResponse.from(result));
    }

    @PostMapping("/login/kakao")
    public ResponseEntity<AuthTokenResponse> kakaoLogin(
            @Valid @RequestBody KakaoLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var result = kakaoLoginUseCase.execute(request.toCommand());
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        applyAuthCookiesIfNeeded(responseBuilder, httpServletRequest, authTokenCookieManager.createAuthCookieHeaders(result));
        return responseBuilder.body(AuthTokenResponse.from(result));
    }

    @GetMapping("/login/kakao/callback")
    public ResponseEntity<Void> kakaoLoginCallback(
            @RequestParam String code,
            HttpServletRequest httpServletRequest
    ) {
        var result = kakaoLoginUseCase.execute(new KakaoLoginCommand(code));
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(302)
                .location(URI.create(kakaoLoginProperties.frontendRedirectUri()));
        applyAuthCookiesIfNeeded(responseBuilder, httpServletRequest, authTokenCookieManager.createAuthCookieHeaders(result));
        return responseBuilder.build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthTokenResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest request,
            HttpServletRequest httpServletRequest
    ) {
        var result = reissueTokenUseCase.execute(
                new RefreshTokenCommand(
                        authTokenCookieManager.resolveRefreshToken(
                                request != null ? request.refreshToken() : null,
                                httpServletRequest
                        )
                )
        );

        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.ok();
        applyAuthCookiesIfNeeded(responseBuilder, httpServletRequest, authTokenCookieManager.createAuthCookieHeaders(result));
        return responseBuilder.body(AuthTokenResponse.from(result));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestBody(required = false) LogoutRequest request,
            HttpServletRequest httpServletRequest
    ) {
        logoutUseCase.execute(
                new LogoutCommand(
                        authTokenCookieManager.resolveRefreshToken(
                                request != null ? request.refreshToken() : null,
                                httpServletRequest
                        )
                )
        );

        ResponseEntity.HeadersBuilder<?> responseBuilder = ResponseEntity.noContent();
        applyAuthCookiesIfNeeded(responseBuilder, httpServletRequest, authTokenCookieManager.clearAuthCookieHeaders());
        return responseBuilder.build();
    }

    private void applyAuthCookiesIfNeeded(
            ResponseEntity.HeadersBuilder<?> responseBuilder,
            HttpServletRequest request,
            List<String> cookieHeaders
    ) {
        if (AuthTransport.from(request).usesCookies()) {
            responseBuilder.header(HttpHeaders.SET_COOKIE, cookieHeaders.toArray(String[]::new));
        }
    }
}
