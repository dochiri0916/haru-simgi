package com.dochiri.authservice.infrastructure.adapter.out.http;

import com.dochiri.authservice.domain.exception.AuthErrorCode;
import com.dochiri.authservice.application.port.out.SocialUserCreatePort;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserCommand;
import com.dochiri.authservice.application.port.out.dto.CreateSocialUserResult;
import com.dochiri.authservice.infrastructure.adapter.out.http.request.CreateSocialUserRequest;
import com.dochiri.authservice.infrastructure.adapter.out.http.response.CreateSocialUserResponse;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.internalapi.InternalRestClient;
import com.dochiri.security.internalapi.InternalRestClient.InternalRpcRequest;
import org.springframework.stereotype.Component;

@Component
public class UserServiceSocialUserCreateAdapter implements SocialUserCreatePort {

    private static final String USER_SERVICE_NAME = "user-service";
    private static final String CREATE_USER_PATH = "/internal/users";

    private final InternalRestClient internalRestClient;

    public UserServiceSocialUserCreateAdapter(InternalRestClient internalRestClient) {
        this.internalRestClient = internalRestClient;
    }

    @Override
    public CreateSocialUserResult create(CreateSocialUserCommand command) {
        CreateSocialUserResponse response = internalRestClient.exchange(
                InternalRpcRequest.post(
                        USER_SERVICE_NAME,
                        CREATE_USER_PATH,
                        new CreateSocialUserRequest(
                                command.idempotencyKey(),
                                command.nickname(),
                                command.profileImageUrl()
                        ),
                        CreateSocialUserResponse.class,
                        AuthErrorCode.USER_SERVICE_UNAVAILABLE
                )
        );

        if (response == null || response.publicId() == null) {
            throw new BaseException(AuthErrorCode.USER_SERVICE_UNAVAILABLE);
        }

        return response.toResult();
    }
}
