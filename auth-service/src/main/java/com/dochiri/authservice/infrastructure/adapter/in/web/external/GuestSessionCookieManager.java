package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.dto.IssueGuestSessionResult;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.infrastructure.configuration.GuestSessionProperties;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.properties.JwtCookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class GuestSessionCookieManager {

    public static final String GUEST_SESSION_COOKIE_NAME = "guest_session";

    private final JwtCookieProperties jwtCookieProperties;
    private final GuestSessionProperties guestSessionProperties;

    public GuestSessionCookieManager(
            JwtCookieProperties jwtCookieProperties,
            GuestSessionProperties guestSessionProperties
    ) {
        this.jwtCookieProperties = jwtCookieProperties;
        this.guestSessionProperties = guestSessionProperties;
    }

    public String createGuestSessionCookieHeader(IssueGuestSessionResult result) {
        return buildCookie(result.token(), guestSessionProperties.expiration().toSeconds()).toString();
    }

    public String clearGuestSessionCookieHeader() {
        return buildCookie("", 0).toString();
    }

    public String resolveGuestSessionToken(HttpServletRequest request) {
        return resolveOptionalGuestSessionToken(request)
                .orElseThrow(() -> new BaseException(AuthErrorCode.INVALID_GUEST_SESSION));
    }

    public Optional<String> resolveOptionalGuestSessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (GUEST_SESSION_COOKIE_NAME.equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {
                    return Optional.of(cookie.getValue());
                }
            }
        }

        return Optional.empty();
    }

    private ResponseCookie buildCookie(String value, long maxAgeSeconds) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(GUEST_SESSION_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(jwtCookieProperties.secure())
                .path("/")
                .sameSite(jwtCookieProperties.sameSite())
                .maxAge(maxAgeSeconds);

        if (StringUtils.hasText(jwtCookieProperties.domain())) {
            builder.domain(jwtCookieProperties.domain());
        }

        return builder.build();
    }
}
