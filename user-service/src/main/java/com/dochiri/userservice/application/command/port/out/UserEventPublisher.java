package com.dochiri.userservice.application.command.port.out;

import com.dochiri.userservice.application.command.event.UserRegisteredEvent;

public interface UserEventPublisher {

    void publishUserRegistered(UserRegisteredEvent event);

}
