package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.application.error.AuthErrorCode;
import com.dochiri.authservice.application.port.out.UserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateUserResult;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.CreateUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.CreateUserResponse;
import com.dochiri.errorhandling.BaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UserServiceUserCreateAdapter implements UserCreatePort {

    private final RestClient restClient;

    public UserServiceUserCreateAdapter(
            RestClient.Builder restClientBuilder,
            @Value("${app.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
    }

    @Override
    public CreateUserResult create(CreateUserCommand command) {
        try {
            CreateUserResponse response = restClient.post()
                    .uri("/internal/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CreateUserRequest(command.email()))
                    .retrieve()
                    .body(CreateUserResponse.class);

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
