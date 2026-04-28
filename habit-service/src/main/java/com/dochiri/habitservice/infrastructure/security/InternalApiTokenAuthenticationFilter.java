package com.dochiri.habitservice.infrastructure.security;

import com.dochiri.habitservice.infrastructure.configuration.InternalApiServerProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class InternalApiTokenAuthenticationFilter extends OncePerRequestFilter {

    public static final String INTERNAL_API_PATH_PREFIX = "/internal/";
    public static final String INTERNAL_API_TOKEN_HEADER = "X-Internal-Api-Token";
    public static final String INTERNAL_API_ROLE = "ROLE_INTERNAL_API";

    private final InternalApiServerProperties internalApiServerProperties;

    public InternalApiTokenAuthenticationFilter(InternalApiServerProperties internalApiServerProperties) {
        this.internalApiServerProperties = internalApiServerProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!isInternalApiRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = request.getHeader(INTERNAL_API_TOKEN_HEADER);
        if (!StringUtils.hasText(token) || !internalApiServerProperties.token().equals(token)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "internal-api",
                null,
                List.of(new SimpleGrantedAuthority(INTERNAL_API_ROLE))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isInternalApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith(INTERNAL_API_PATH_PREFIX);
    }
}
