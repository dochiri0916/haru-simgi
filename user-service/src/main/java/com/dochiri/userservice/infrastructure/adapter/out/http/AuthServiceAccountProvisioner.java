package com.dochiri.userservice.infrastructure.adapter.out.http;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.role.UserRole;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.out.AuthAccountProvisioner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class AuthServiceAccountProvisioner implements AuthAccountProvisioner {

    private final RestClient restClient;

    public AuthServiceAccountProvisioner(
            RestClient.Builder restClientBuilder,
            @Value("${app.auth-service.base-url:http://localhost:8082}") String authServiceBaseUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(authServiceBaseUrl)
                .build();
    }

    @Override
    public void provision(Long userId, String publicId, String email, String passwordHash, UserRole role) {
        try {
            restClient.post()
                    .uri("/internal/auth-users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ProvisionAuthAccountRequest(userId, publicId, email, passwordHash, role))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new BaseException(UserErrorCode.AUTH_ACCOUNT_SYNC_FAILED, exception);
        }
    }
}
