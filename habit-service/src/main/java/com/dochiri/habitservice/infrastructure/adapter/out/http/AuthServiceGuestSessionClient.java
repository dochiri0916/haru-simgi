package com.dochiri.habitservice.infrastructure.adapter.out.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Component
public class AuthServiceGuestSessionClient implements GuestSessionClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceGuestSessionClient.class);

    private static final String AUTH_SERVICE_NAME = "auth-service";
    private static final String GUEST_SESSION_COOKIE_NAME = "guest_session";

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancerClient;

    public AuthServiceGuestSessionClient(LoadBalancerClient loadBalancerClient) {
        this.restClient = RestClient.builder().build();
        this.loadBalancerClient = loadBalancerClient;
    }

    @Override
    public Optional<GuestSessionResult> getGuestSession(String guestSessionToken) {
        if (!StringUtils.hasText(guestSessionToken)) {
            return Optional.empty();
        }

        ServiceInstance instance = loadBalancerClient.choose(AUTH_SERVICE_NAME);
        if (instance == null) {
            log.warn("auth-service 인스턴스를 찾을 수 없어 게스트 세션을 검증할 수 없습니다.");
            return Optional.empty();
        }

        try {
            GuestSessionResult response = restClient.get()
                    .uri(instance.getUri() + "/api/auth/guest/me")
                    .header(HttpHeaders.COOKIE, GUEST_SESSION_COOKIE_NAME + "=" + guestSessionToken)
                    .retrieve()
                    .body(GuestSessionResult.class);

            if (response == null || !StringUtils.hasText(response.guestId()) || !response.active()) {
                return Optional.empty();
            }

            return Optional.of(response);
        } catch (RestClientResponseException exception) {
            log.debug("게스트 세션 검증 실패: status={}", exception.getStatusCode());
            return Optional.empty();
        } catch (RestClientException exception) {
            log.warn("게스트 세션 검증 중 auth-service 호출 실패", exception);
            return Optional.empty();
        }
    }
}
