package com.dochiri.userservice.infrastructure.adapter.out.messaging.kafka;

import com.dochiri.userservice.application.command.event.UserRegisteredEvent;
import com.dochiri.userservice.application.command.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisher implements UserEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        kafkaTemplate.send(
                kafkaTopicsProperties.userRegistered(),
                event.publicId(),
                UserRegisteredMessage.from(event)
        );
    }
}
