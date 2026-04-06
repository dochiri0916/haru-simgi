package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.properties.JwtCookieProperties;
import com.dochiri.security.properties.JwtProperties;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthTokenCookieManagerTest {

    private AuthTokenCookieManager authTokenCookieManager;

    @BeforeEach
    void setUp() {
        authTokenCookieManager = new AuthTokenCookieManager(
                new JwtCookieProperties(null, null, null, null, null, null, false),
                new JwtProperties("12345678901234567890123456789012", 1_800_000L, 1_209_600_000L)
        );
    }

    @Test
    void 로그인_응답용_쿠키_헤더를_생성한다() {
        AuthTokenResult result = new AuthTokenResult(
                "access-token",
                "refresh-token",
                Instant.now().plusSeconds(60),
                UserRole.USER
        );

        var headers = authTokenCookieManager.createAuthCookieHeaders(result);

        assertThat(headers).hasSize(2);
        assertThat(headers.get(0)).contains("access_token=access-token");
        assertThat(headers.get(0)).contains("HttpOnly");
        assertThat(headers.get(1)).contains("refresh_token=refresh-token");
        assertThat(headers.get(1)).contains("Path=/api/auth");
    }

    @Test
    void 요청_바디에_토큰이_있으면_그값을_우선한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String token = authTokenCookieManager.resolveRefreshToken("body-refresh-token", request);

        assertThat(token).isEqualTo("body-refresh-token");
    }

    @Test
    void 요청_바디가_없으면_refresh_token_쿠키를_사용한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Cookie", "refresh_token=cookie-refresh-token");
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "cookie-refresh-token"));

        String token = authTokenCookieManager.resolveRefreshToken(null, request);

        assertThat(token).isEqualTo("cookie-refresh-token");
    }

    @Test
    void refresh_token이_없으면_INVALID_REFRESH_TOKEN_예외가_발생한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> authTokenCookieManager.resolveRefreshToken(null, request))
                .isInstanceOf(BaseException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }
}
