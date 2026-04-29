package com.dochiri.habitservice.infrastructure.adapter.out.http;

import com.dochiri.errorhandling.CommonErrorCode;
import com.dochiri.security.internalapi.InternalRestClient;
import com.dochiri.security.internalapi.InternalRestClient.InternalRpcRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class AuthServiceGuestSessionClient implements GuestSessionClient {

    private static final String AUTH_SERVICE_NAME = "auth-service";
    private static final String VERIFY_PATH = "/internal/guest-sessions/verify";

    private final InternalRestClient internalRestClient;

    public AuthServiceGuestSessionClient(InternalRestClient internalRestClient) {
        this.internalRestClient = internalRestClient;
    }

    @Override
    public Optional<GuestSessionResult> getGuestSession(String guestSessionToken) {
        if (!StringUtils.hasText(guestSessionToken)) {
            return Optional.empty();
        }

        return internalRestClient.tryExchange(
                        InternalRpcRequest.post(
                                AUTH_SERVICE_NAME,
                                VERIFY_PATH,
                                new VerifyGuestSessionRequest(guestSessionToken),
                                GuestSessionResult.class,
                                CommonErrorCode.INTERNAL_SERVER_ERROR
                        )
                )
                .filter(result -> StringUtils.hasText(result.guestId()) && result.active());
    }

    record VerifyGuestSessionRequest(String token) {
    }
}
