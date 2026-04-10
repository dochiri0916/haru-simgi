package com.dochiri.security.role;

import org.springframework.security.authentication.BadCredentialsException;

import java.util.Arrays;

public enum UserRole {
    USER,
    ADMIN;

    public static UserRole from(String value) {
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("유효한 role 값이 필요합니다.");
        }

        return Arrays.stream(values())
                .filter(role -> role.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new BadCredentialsException("지원하지 않는 role 값입니다."));
    }
}