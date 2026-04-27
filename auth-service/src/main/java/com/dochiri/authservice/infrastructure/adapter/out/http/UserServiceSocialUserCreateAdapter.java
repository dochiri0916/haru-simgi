package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.out.SocialUserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.CreateSocialUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.CreateSocialUserResponse;
import com.dochiri.authservice.infrastructure.configuration.InternalApiClientProperties;
import com.dochiri.errorhandling.BaseException;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceSocialUserCreateAdapter implements SocialUserCreatePort {

    static final String USER_SERVICE_BASE_URL = "lb://user-service";
    static final String INTERNAL_API_TOKEN_HEADER = "X-Internal-Api-Token";

    private final RestClient restClient;
    private final String internalApiToken;

    public UserServiceSocialUserCreateAdapter(
            @LoadBalanced RestClient.Builder restClientBuilder,
            InternalApiClientProperties internalApiClientProperties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(USER_SERVICE_BASE_URL)
                .build();
        this.internalApiToken = internalApiClientProperties.token();
    }

    @Override
    public CreateSocialUserResult create(CreateSocialUserCommand command) {
        try {
            CreateSocialUserResponse response = restClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(INTERNAL_API_TOKEN_HEADER, internalApiToken)
                    .body(new CreateSocialUserRequest(
                            command.idempotencyKey(),
                            command.nickname(),
                            command.profileImageUrl()
                    ))
                    .retrieve()
                    .body(CreateSocialUserResponse.class);

            if (response == null || response.publicId() == null) {
                throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
            }

            return response.toResult();
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE, exception);
        }
    }
}
