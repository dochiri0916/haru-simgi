package com.dochiri.habitservice.infrastructure.security;

import com.dochiri.habitservice.infrastructure.adapter.out.http.GuestSessionClient;
import com.dochiri.habitservice.infrastructure.adapter.out.http.GuestSessionResult;
import com.dochiri.security.jwt.JwtPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GuestSessionAuthenticationFilterTest {

    private final GuestSessionClient guestSessionClient = mock(GuestSessionClient.class);
    private final GuestSessionAuthenticationFilter filter = new GuestSessionAuthenticationFilter(guestSessionClient);
    private final FilterChain filterChain = mock(FilterChain.class);

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void JWT_인증이_없고_유효한_게스트_쿠키가_있으면_게스트_인증을_설정한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("guest_session", "raw-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(guestSessionClient.getGuestSession("raw-token"))
                .thenReturn(Optional.of(new GuestSessionResult("guest-public-id", "ACTIVE", Instant.now().plusSeconds(60))));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new GuestPrincipal("guest-public-id"));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void JWT_인증이_이미_있으면_게스트_세션을_검증하지_않는다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("guest_session", "raw-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new JwtPrincipal("user-public-id", "USER"), null, List.of())
        );

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isEqualTo(new JwtPrincipal("user-public-id", "USER"));
        verify(guestSessionClient, never()).getGuestSession("raw-token");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void 게스트_쿠키가_없으면_인증을_설정하지_않는다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(guestSessionClient, never()).getGuestSession("raw-token");
        verify(filterChain).doFilter(request, response);
    }
}
