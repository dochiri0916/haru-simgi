package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.SocialUserProvisionPort;
import com.dochiri.authservice.application.port.out.dto.ProvisionedSocialUser;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.ProvisionSocialUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.ProvisionSocialUserResponse;
import com.dochiri.errorhandling.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class UserServiceSocialUserProvisionAdapter implements SocialUserProvisionPort {

    private final RestClient restClient;

    public UserServiceSocialUserProvisionAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Override
    public ProvisionedSocialUser provision(String email, String nickname, String profileImageUrl) {
        try {
            ProvisionSocialUserResponse response = restClient.post()
                    .uri("/internal/users/social")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ProvisionSocialUserRequest(email, nickname, profileImageUrl))
                    .retrieve()
                    .body(ProvisionSocialUserResponse.class);

            if (response == null || response.userId() == null) {
                throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
            }

            return response.toResult();
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE, exception);
        }
    }
}
