package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserRegisteredConsumerTest {

    private final SyncAuthUserUseCase syncAuthUserUseCase = mock(SyncAuthUserUseCase.class);
    private final UserRegisteredConsumer userRegisteredConsumer = new UserRegisteredConsumer(syncAuthUserUseCase);

    @Test
    void 회원가입_이벤트를_받으면_인증_유저를_동기화한다() {
        UserRegisteredMessage message = new UserRegisteredMessage(
                1L,
                "user-public-id",
                "alice@example.com",
                "$2a$10$zszJ0gQPr0K1Q8v75eA0vOq4J2Jix.vAU6YewsGQKzBSSMSmc5Qw2",
                "USER"
        );

        userRegisteredConsumer.consume(message);

        verify(syncAuthUserUseCase).sync(
                new SyncAuthUserCommand(
                        1L,
                        "user-public-id",
                        "alice@example.com",
                        "$2a$10$zszJ0gQPr0K1Q8v75eA0vOq4J2Jix.vAU6YewsGQKzBSSMSmc5Qw2",
                        "USER"
                )
        );
    }
}
