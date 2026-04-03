package com.dochiri.userservice.application.command.service;

import com.dochiri.userservice.application.command.event.UserRegisteredEvent;
import com.dochiri.userservice.application.command.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.command.port.out.UserEventPublisher;
import com.dochiri.userservice.application.command.port.out.UserProjection;
import com.dochiri.userservice.application.command.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserEventPublisher userEventPublisher = mock(UserEventPublisher.class);

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(userRepository, userEventPublisher);
    }

    @Test
    void 회원가입에_성공하면_유저_등록_이벤트를_발행한다() {
        RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "secret123");
        User savedUser = User.from("user-public-id", "alice@example.com");

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.loadProjectionByEmail(savedUser.getEmail()))
                .thenReturn(new UserProjection(1L, "user-public-id", "alice@example.com", "USER"));

        registerUserService.register(command);

        verify(userEventPublisher).publishUserRegistered(
                new UserRegisteredEvent(1L, "user-public-id", "alice@example.com", "secret123", "USER")
        );
    }
}
