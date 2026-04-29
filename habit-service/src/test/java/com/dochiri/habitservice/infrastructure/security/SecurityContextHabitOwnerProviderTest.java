package com.dochiri.habitservice.infrastructure.security;

import com.dochiri.habitservice.domain.habit.OwnerType;
import com.dochiri.security.jwt.JwtPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextHabitOwnerProviderTest {

    private final SecurityContextHabitOwnerProvider provider = new SecurityContextHabitOwnerProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void JWT_인증이면_USER_owner를_반환한다() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtPrincipal("user-public-id", "USER"), null, List.of())
        );

        var owner = provider.currentOwner();

        assertThat(owner.type()).isEqualTo(OwnerType.USER);
        assertThat(owner.ownerId()).isEqualTo("user-public-id");
    }

    @Test
    void 게스트_인증이면_GUEST_owner를_반환한다() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new GuestPrincipal("guest-public-id"), null, List.of())
        );

        var owner = provider.currentOwner();

        assertThat(owner.type()).isEqualTo(OwnerType.GUEST);
        assertThat(owner.ownerId()).isEqualTo("guest-public-id");
    }

    @Test
    void 인증이_없으면_예외가_발생한다() {
        assertThatThrownBy(provider::currentOwner)
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
