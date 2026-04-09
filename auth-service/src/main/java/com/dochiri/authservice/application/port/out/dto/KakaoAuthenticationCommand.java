package com.dochiri.authservice.application.port.out.dto;

public record KakaoAuthenticationCommand(
        String authorizationCode
) {
}
