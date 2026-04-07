package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.UserProvisionPort;
import com.dochiri.authservice.application.port.out.dto.ProvisionedUser;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.ProvisionUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.ProvisionUserResponse;
import com.dochiri.errorhandling.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserServiceUserProvisionAdapter implements UserProvisionPort {

    private final RestClient restClient;

    public UserServiceUserProvisionAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Override
    public ProvisionedUser provision(String email) {
        try {
            ProvisionUserResponse response = restClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ProvisionUserRequest(email))
                    .retrieve()
                    .body(ProvisionUserResponse.class);

            if (response == null || response.userId() == null) {
                throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
            }

            return response.toResult();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 409) {
                throw new BaseException(AuthErrorCode.DUPLICATE_EMAIL, exception);
            }

            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE, exception);
        } catch (RestClientException exception) {
            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE, exception);
        }
    }

}
