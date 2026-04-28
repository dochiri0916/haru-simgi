package com.dochiri.habitservice.infrastructure.security;

import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.security.jwt.JwtPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class HabitOwnerResolver {

    public HabitOwner resolve() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("습관 소유자를 확인할 수 없습니다.");
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtPrincipal jwtPrincipal) {
            return HabitOwner.user(jwtPrincipal.publicId());
        }
        if (principal instanceof GuestPrincipal guestPrincipal) {
            return HabitOwner.guest(guestPrincipal.guestId());
        }

        throw new AuthenticationCredentialsNotFoundException("지원하지 않는 인증 주체입니다.");
    }
}
