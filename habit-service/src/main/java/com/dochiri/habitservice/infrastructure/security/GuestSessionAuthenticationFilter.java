package com.dochiri.habitservice.infrastructure.security;

import com.dochiri.habitservice.infrastructure.adapter.out.http.GuestSessionClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class GuestSessionAuthenticationFilter extends OncePerRequestFilter {

    static final String GUEST_SESSION_COOKIE_NAME = "guest_session";

    private final GuestSessionClient guestSessionClient;

    public GuestSessionAuthenticationFilter(GuestSessionClient guestSessionClient) {
        this.guestSessionClient = guestSessionClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            resolveGuestSessionToken(request)
                    .flatMap(guestSessionClient::getGuestSession)
                    .ifPresent(guestSession -> SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    new GuestPrincipal(guestSession.guestId()),
                                    null,
                                    List.of(new SimpleGrantedAuthority("ROLE_GUEST"))
                            )
                    ));
        }

        filterChain.doFilter(request, response);
    }

    private java.util.Optional<String> resolveGuestSessionToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return java.util.Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (GUEST_SESSION_COOKIE_NAME.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                return java.util.Optional.of(cookie.getValue());
            }
        }

        return java.util.Optional.empty();
    }
}
