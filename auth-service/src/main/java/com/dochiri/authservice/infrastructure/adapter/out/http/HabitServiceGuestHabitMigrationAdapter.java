package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.port.out.GuestHabitMigrationPort;
import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.MigrateGuestHabitsRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.MigrateGuestHabitsResponse;
import com.dochiri.authservice.infrastructure.configuration.InternalApiClientProperties;
import com.dochiri.errorhandling.BaseException;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class HabitServiceGuestHabitMigrationAdapter implements GuestHabitMigrationPort {

    private static final String HABIT_SERVICE_NAME = "habit-service";
    private static final String INTERNAL_API_TOKEN_HEADER = "X-Internal-Api-Token";

    private final RestClient restClient;
    private final LoadBalancerClient loadBalancerClient;
    private final String internalApiToken;

    public HabitServiceGuestHabitMigrationAdapter(
            LoadBalancerClient loadBalancerClient,
            InternalApiClientProperties internalApiClientProperties
    ) {
        this.restClient = RestClient.builder().build();
        this.loadBalancerClient = loadBalancerClient;
        this.internalApiToken = internalApiClientProperties.token();
    }

    @Override
    public int migrate(String guestId, String userPublicId) {
        ServiceInstance instance = loadBalancerClient.choose(HABIT_SERVICE_NAME);
        if (instance == null) {
            throw new BaseException(AuthErrorCode.HABIT_SERVICE_UNAVAILABLE);
        }

        try {
            MigrateGuestHabitsResponse response = restClient.patch()
                    .uri(instance.getUri() + "/internal/habits/guest-owner")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(INTERNAL_API_TOKEN_HEADER, internalApiToken)
                    .body(new MigrateGuestHabitsRequest(guestId, userPublicId))
                    .retrieve()
                    .body(MigrateGuestHabitsResponse.class);

            if (response == null) {
                throw new BaseException(AuthErrorCode.HABIT_SERVICE_UNAVAILABLE);
            }

            return response.migratedCount();
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.HABIT_SERVICE_UNAVAILABLE, exception);
        }
    }
}
