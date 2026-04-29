package com.dochiri.security.jwt;

import com.dochiri.security.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RefreshTokenVerifierTest {

    private static final String SECRET = "test-secret-key-that-is-at-least-32-characters-long";

    private JwtProvider jwtProvider;
    private RefreshTokenVerifier verifier;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(new JwtProperties(SECRET, 3600000L, 604800000L));
        verifier = new RefreshTokenVerifier(jwtProvider);
    }

    @Test
    void 유효한_리프레시_토큰에서_userId를_추출할_수_있다() {
        String refreshToken = jwtProvider.generateRefreshToken("public-id-42", "USER");

        String publicId = verifier.verifyAndExtractPublicId(refreshToken);

        assertThat(publicId).isEqualTo("public-id-42");
    }

    @Test
    void 액세스_토큰으로_검증하면_BadCredentialsException이_발생한다() {
        String accessToken = jwtProvider.generateAccessToken("public-id-1", "USER");

        assertThatThrownBy(() -> verifier.verifyAndExtractPublicId(accessToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("리프레시 토큰");
    }

    @Test
    void 만료된_리프레시_토큰으로_검증하면_예외가_발생한다() {
        JwtProvider expiredProvider = new JwtProvider(new JwtProperties(SECRET, 0L, 0L));
        String expiredToken = expiredProvider.generateRefreshToken("public-id-1", "USER");

        assertThatThrownBy(() -> verifier.verifyAndExtractPublicId(expiredToken))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("만료된 JWT");
    }
}
