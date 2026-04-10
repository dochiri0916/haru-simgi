package com.dochiri.security.autoconfigure;

import com.dochiri.security.jwt.*;
import com.dochiri.security.properties.JwtCookieProperties;
import com.dochiri.security.properties.JwtProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "jwt", name = {"secret", "access-expiration", "refresh-expiration"})
@EnableConfigurationProperties({JwtProperties.class, JwtCookieProperties.class})
class JwtAutoConfiguration {

    @Bean
    JwtProvider jwtProvider(JwtProperties jwtProperties, ObjectProvider<Clock> clockProvider) {
        return new JwtProvider(jwtProperties, clockProvider.getIfAvailable(Clock::systemUTC));
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(JwtProvider jwtProvider) {
        return new JwtAuthenticationConverter(jwtProvider);
    }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            JwtCookieProperties jwtCookieProperties
    ) {
        return new JwtAuthenticationFilter(jwtAuthenticationConverter, jwtCookieProperties);
    }

    @Bean
    JwtTokenGenerator jwtTokenGenerator(JwtProvider jwtProvider) {
        return new JwtTokenGenerator(jwtProvider);
    }

    @Bean
    RefreshTokenVerifier refreshTokenVerifier(JwtProvider jwtProvider) {
        return new RefreshTokenVerifier(jwtProvider);
    }

}
