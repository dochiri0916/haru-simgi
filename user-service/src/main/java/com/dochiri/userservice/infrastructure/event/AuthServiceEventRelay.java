package com.dochiri.userservice.infrastructure.event;

import com.dochiri.userservice.application.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthServiceEventRelay {

    private final RestClient authServiceRestClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        authServiceRestClient.post()
                .uri("/api/internal/auth/users")
                .body(AuthUserSyncRequest.from(event))
                .retrieve()
                .toBodilessEntity();
    }
}
