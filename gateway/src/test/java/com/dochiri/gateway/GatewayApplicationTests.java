package com.dochiri.gateway;

import com.dochiri.gateway.config.GatewayCorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.web.cors.reactive.CorsWebFilter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false"
})
class GatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Autowired
    private GatewayCorsProperties gatewayCorsProperties;

    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void 게이트웨이_라우트_ID를_검증한다() {

        // given
        // Gateway가 정상적으로 기동되어 라우트가 등록된 상태

        // when
        List<String> routeIds = routeLocator.getRoutes()
                .map(Route::getId)
                .collectList()
                .block();

        // then
        assertThat(routeIds)
                .contains("auth-service", "auth-admin-service", "user-service", "habit-service");
    }

    @Test
    void CORS_프로퍼티_바인딩과_필터_생성을_검증한다() {

        // given
        // application.yml에 정의된 CORS 설정이 존재

        // when
        List<String> allowedOrigins = gatewayCorsProperties.allowedOrigins();
        boolean allowCredentials = gatewayCorsProperties.allowCredentials();

        // then
        assertThat(allowedOrigins)
                .contains("http://localhost:3000", "http://localhost:5173");
        assertThat(allowCredentials).isTrue();
        assertThat(corsWebFilter).isNotNull();
    }

}
