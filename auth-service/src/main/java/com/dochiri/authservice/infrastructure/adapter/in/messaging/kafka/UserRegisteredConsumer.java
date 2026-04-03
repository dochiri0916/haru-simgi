package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredConsumer {

    private final SyncAuthUserUseCase syncAuthUserUseCase;

    @KafkaListener(
            topics = "${app.kafka.topics.user-registered}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(UserRegisteredMessage message) {
        syncAuthUserUseCase.sync(message.toCommand());
    }
}
