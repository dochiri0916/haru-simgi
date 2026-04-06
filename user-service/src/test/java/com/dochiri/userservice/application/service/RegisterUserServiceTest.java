package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.event.UserRegisteredEvent;
import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.out.UserEventPublisher;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegisterUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserEventPublisher userEventPublisher = mock(UserEventPublisher.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private RegisterUserService registerUserService;

    @BeforeEach
    void setUp() {
        registerUserService = new RegisterUserService(userRepository, userEventPublisher, passwordEncoder);
    }

    @Test
    void 회원가입에_성공하면_유저_등록_이벤트를_발행한다() {
        RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "secret123");
        User savedUser = User.from("user-public-id", "alice@example.com");

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        registerUserService.register(command);

        ArgumentCaptor<UserRegisteredEvent> eventCaptor = ArgumentCaptor.forClass(UserRegisteredEvent.class);
        verify(userEventPublisher).publishUserRegistered(eventCaptor.capture());

        UserRegisteredEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isNull();
        assertThat(event.publicId()).isEqualTo("user-public-id");
        assertThat(event.email()).isEqualTo("alice@example.com");
        assertThat(event.passwordHash()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", event.passwordHash())).isTrue();
        assertThat(event.role()).isEqualTo("USER");
    }
}
