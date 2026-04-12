package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.properties.JwtCookieProperties;
import com.dochiri.security.properties.JwtProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;

@Component
public class AuthTokenCookieManager {

    private final JwtCookieProperties jwtCookieProperties;
    private final JwtProperties jwtProperties;

    public AuthTokenCookieManager(JwtCookieProperties jwtCookieProperties, JwtProperties jwtProperties) {
        this.jwtCookieProperties = jwtCookieProperties;
        this.jwtProperties = jwtProperties;
    }

    public List<String> createAuthCookieHeaders(IssueAuthTokenResult result) {
        return List.of(
                buildCookie(
                        jwtCookieProperties.accessTokenName(),
                        result.accessToken(),
                        jwtCookieProperties.accessTokenPath(),
                        Duration.ofMillis(jwtProperties.accessExpiration())
                ).toString(),
                buildCookie(
                        jwtCookieProperties.refreshTokenName(),
                        result.refreshToken(),
                        jwtCookieProperties.refreshTokenPath(),
                        Duration.ofMillis(jwtProperties.refreshExpiration())
                ).toString()
        );
    }

    public List<String> clearAuthCookieHeaders() {
        return List.of(
                buildCookie(jwtCookieProperties.accessTokenName(), "", jwtCookieProperties.accessTokenPath(), Duration.ZERO).toString(),
                buildCookie(jwtCookieProperties.refreshTokenName(), "", jwtCookieProperties.refreshTokenPath(), Duration.ZERO).toString()
        );
    }

    public String resolveRefreshToken(String refreshToken, HttpServletRequest request) {
        if (StringUtils.hasText(refreshToken)) {
            return refreshToken;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (jwtCookieProperties.refreshTokenName().equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {
                    return cookie.getValue();
                }
            }
        }

        throw new BaseException(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    private ResponseCookie buildCookie(String name, String value, String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(jwtCookieProperties.secure())
                .path(path)
                .sameSite(jwtCookieProperties.sameSite())
                .maxAge(maxAge);

        if (StringUtils.hasText(jwtCookieProperties.domain())) {
            builder.domain(jwtCookieProperties.domain());
        }

        return builder.build();
    }
}
