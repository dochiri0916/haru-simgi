package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.SocialUserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.CreateSocialUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.CreateSocialUserResponse;
import com.dochiri.errorhandling.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceSocialUserCreateAdapter implements SocialUserCreatePort {

    private final RestClient restClient;

    public UserServiceSocialUserCreateAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Override
    public CreateSocialUserResult create(CreateSocialUserCommand command) {
        try {
            CreateSocialUserResponse response = restClient.post()
                    .uri("/internal/users/social")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateSocialUserRequest(command.nickname(), command.profileImageUrl()))
                    .retrieve()
                    .body(CreateSocialUserResponse.class);

            if (response == null || response.userId() == null) {
                throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
            }

            return response.toResult();
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE, exception);
        }
    }
}
