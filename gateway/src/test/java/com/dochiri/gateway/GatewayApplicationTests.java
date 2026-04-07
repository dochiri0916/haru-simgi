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
    void given_게이트웨이_라우트가_주어지면_when_라우트_정의를_로딩할_때_then_기대한_라우트_ID를_포함한다() {
        // given

        // when
        List<String> routeIds = routeLocator.getRoutes()
                .map(Route::getId)
                .collectList()
                .block();

        // then
        assertThat(routeIds)
                .contains("auth-service", "auth-admin-service", "user-service", "task-service");
    }

    @Test
    void given_로컬_프론트엔드_CORS_설정이_주어지면_when_게이트웨이_CORS_프로퍼티를_바인딩할_때_then_자격증명과_허용_출처가_구성된다() {
        // given

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
