package com.dochiri.authservice.infrastructure.security;

import com.dochiri.security.internalapi.InternalApiTokenAuthenticationFilter;
import com.dochiri.security.jwt.JwtAuthenticationFilter;
import com.dochiri.security.properties.SecurityProperties;
import com.dochiri.security.web.JwtAccessDeniedHandler;
import com.dochiri.security.web.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration(proxyBeanMethods = false)
public class AuthServiceSecurityConfiguration {

    @Bean
    SecurityFilterChain authServiceSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            InternalApiTokenAuthenticationFilter internalApiTokenAuthenticationFilter,
            JwtAccessDeniedHandler jwtAccessDeniedHandler,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            SecurityProperties securityProperties
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(jwtAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(securityProperties.publicEndpoints().toArray(String[]::new))
                        .permitAll()
                        .requestMatchers(InternalApiTokenAuthenticationFilter.INTERNAL_API_PATH_PREFIX + "**")
                        .hasRole("INTERNAL_API")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(internalApiTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults())
                .build();
    }
}
