package com.dochiri.userservice.infrastructure.adapter.out.messaging.kafka;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.event.UserRegisteredEvent;
import com.dochiri.userservice.application.error.UserErrorCode;
import com.dochiri.userservice.application.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class KafkaUserEventPublisher implements UserEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaTopicsProperties kafkaTopicsProperties;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        try {
            kafkaTemplate.send(
                    kafkaTopicsProperties.userRegistered(),
                    event.publicId(),
                    UserRegisteredMessage.from(event)
            ).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BaseException(UserErrorCode.USER_REGISTERED_EVENT_PUBLISH_FAILED, exception);
        } catch (ExecutionException exception) {
            throw new BaseException(UserErrorCode.USER_REGISTERED_EVENT_PUBLISH_FAILED, exception);
        }
    }

}
