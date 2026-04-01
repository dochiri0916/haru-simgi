package com.dochiri.userservice.infrastructure.event;

import com.dochiri.userservice.application.event.UserRegisteredEvent;
import com.dochiri.userservice.application.port.out.UserEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringUserEventPublisher implements UserEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishUserRegistered(UserRegisteredEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
