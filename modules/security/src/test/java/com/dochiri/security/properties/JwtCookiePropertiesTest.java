package com.dochiri.security.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCookiePropertiesTest {

    @Test
    void 값이_없으면_기본_쿠키_설정이_적용된다() {
        JwtCookieProperties properties = new JwtCookieProperties(null, null, null, null, null, null, false);

        assertThat(properties.accessTokenName()).isEqualTo("access_token");
        assertThat(properties.refreshTokenName()).isEqualTo("refresh_token");
        assertThat(properties.accessTokenPath()).isEqualTo("/");
        assertThat(properties.refreshTokenPath()).isEqualTo("/api/auth");
        assertThat(properties.sameSite()).isEqualTo("Lax");
        assertThat(properties.domain()).isNull();
        assertThat(properties.secure()).isFalse();
    }
}
