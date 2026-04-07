package com.dochiri.authservice.infrastructure.config;

import jakarta.validation.constraints.AssertTrue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import org.springframework.util.StringUtils;

@Validated
@ConfigurationProperties(prefix = "app.kakao")
public record KakaoLoginProperties(
        boolean enabled,
        String restApiKey,
        String clientSecret,
        String redirectUri,
        String frontendRedirectUri,
        String authorizeUri,
        String tokenUri,
        String userInfoUri
) {
    @AssertTrue(message = "카카오 로그인이 활성화되면 restApiKey, redirectUri, authorizeUri, tokenUri, userInfoUri가 필요합니다.")
    public boolean isValidWhenEnabled() {
        return !enabled || (
                StringUtils.hasText(restApiKey)
                        && StringUtils.hasText(redirectUri)
                        && StringUtils.hasText(authorizeUri)
                        && StringUtils.hasText(tokenUri)
                        && StringUtils.hasText(userInfoUri)
        );
    }
}
