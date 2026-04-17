package com.dochiri.gateway.filter;

import com.dochiri.gateway.config.AuthSessionRedisKeyProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class AuthRequiredGatewayFilterFactory
        extends AbstractGatewayFilterFactory<AuthRequiredGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(AuthRequiredGatewayFilterFactory.class);

    private static final String CLAIM_CATEGORY = "category";
    private static final String CATEGORY_ACCESS = "access";

    private final SecretKey signingKey;
    private final String accessTokenCookieName;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AuthSessionRedisKeyProperties redisKeyProperties;
    private final ObjectMapper objectMapper;

    public AuthRequiredGatewayFilterFactory(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.cookie.access-token-name:access_token}") String accessTokenCookieName,
            ReactiveStringRedisTemplate redisTemplate,
            AuthSessionRedisKeyProperties redisKeyProperties,
            ObjectMapper objectMapper
    ) {
        super(Config.class);
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenCookieName = accessTokenCookieName;
        this.redisTemplate = redisTemplate;
        this.redisKeyProperties = redisKeyProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            String token = extractToken(request);
            if (token == null) {
                log.debug("인증 토큰 없음: path={}", request.getPath());
                return writeUnauthorized(exchange.getResponse(), "인증이 필요합니다.");
            }

            Claims claims;
            try {
                claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
            } catch (ExpiredJwtException e) {
                log.debug("만료된 토큰: path={}", request.getPath());
                return writeUnauthorized(exchange.getResponse(), "만료된 토큰입니다.");
            } catch (JwtException | IllegalArgumentException e) {
                log.warn("유효하지 않은 토큰: path={}", request.getPath());
                return writeUnauthorized(exchange.getResponse(), "유효하지 않은 토큰입니다.");
            }

            String sessionId = claims.getId();
            if (!StringUtils.hasText(sessionId)) {
                log.warn("jti 클레임 없음: path={}", request.getPath());
                return writeUnauthorized(exchange.getResponse(), "유효하지 않은 세션입니다.");
            }

            if (!CATEGORY_ACCESS.equals(claims.get(CLAIM_CATEGORY, String.class))) {
                log.debug("액세스 토큰 아님: path={}", request.getPath());
                return writeUnauthorized(exchange.getResponse(), "인증에 사용할 수 없는 토큰입니다.");
            }

            return redisTemplate.hasKey(redisKeyProperties.session() + sessionId)
                    .flatMap(exists -> {
                        if (!exists) {
                            log.debug("세션 없음: sessionId={}", sessionId);
                            return writeUnauthorized(exchange.getResponse(), "만료된 세션입니다.");
                        }
                        return chain.filter(exchange);
                    })
                    .onErrorResume(exception -> {
                        log.error("Redis 세션 확인 실패: path={}", request.getPath(), exception);
                        return writeErrorResponse(exchange.getResponse(), HttpStatus.SERVICE_UNAVAILABLE,
                                "Service Unavailable", "서비스를 일시적으로 사용할 수 없습니다.");
                    });
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
        return writeErrorResponse(response, HttpStatus.UNAUTHORIZED, "Unauthorized", detail);
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, HttpStatus status,
                                          String title, String detail) {
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("title", title);
        body.put("detail", detail);

        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            json = "{\"status\":" + status.value() + ",\"title\":\"" + title + "\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {}
}
