package com.dochiri.userservice.application.port.out;

import com.dochiri.userservice.application.event.UserRegisteredEvent;

public interface UserEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);

}
