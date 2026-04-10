package com.dochiri.gateway.filter;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class AuthRequiredGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {

    private final SecretKey signingKey;
    private final String accessTokenCookieName;

    public AuthRequiredGatewayFilterFactory(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.cookie.access-token-name:access_token}") String accessTokenCookieName
    ) {
        super(Object.class);
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenCookieName = accessTokenCookieName;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String token = extractToken(request);
            if (token == null) {
                return writeUnauthorized(exchange.getResponse(), "인증이 필요합니다.");
            }

            try {
                Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token);
            } catch (ExpiredJwtException e) {
                return writeUnauthorized(exchange.getResponse(), "만료된 토큰입니다.");
            } catch (JwtException | IllegalArgumentException e) {
                return writeUnauthorized(exchange.getResponse(), "유효하지 않은 토큰입니다.");
            }

            return chain.filter(exchange);
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        var cookie = request.getCookies().getFirst(accessTokenCookieName);
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            return cookie.getValue();
        }

        return null;
    }

    private Mono<Void> writeUnauthorized(ServerHttpResponse response, String detail) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"status\":401,\"title\":\"Unauthorized\",\"detail\":\"" + detail + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
