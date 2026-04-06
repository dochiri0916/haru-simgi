package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.userservice.application.port.out.AuthAccountProvisioner;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.security.role.UserRole;
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
    private final AuthAccountProvisioner authAccountProvisioner = mock(AuthAccountProvisioner.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final RegisterUserService registerUserService =
            new RegisterUserService(userRepository, authAccountProvisioner, passwordEncoder);

    @Test
    void 회원가입에_성공하면_인증_계정을_동기_생성한다() {
        RegisterUserCommand command = new RegisterUserCommand("alice@example.com", "secret123");

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(userRepository.save(any())).thenReturn(1L);

        registerUserService.register(command);

        ArgumentCaptor<com.dochiri.userservice.domain.User> userCaptor =
                ArgumentCaptor.forClass(com.dochiri.userservice.domain.User.class);
        verify(userRepository).save(userCaptor.capture());

        String publicId = userCaptor.getValue().getPublicId();
        var passwordHashCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(authAccountProvisioner).provision(
                org.mockito.Mockito.eq(1L),
                org.mockito.Mockito.eq(publicId),
                org.mockito.Mockito.eq("alice@example.com"),
                passwordHashCaptor.capture(),
                org.mockito.Mockito.eq(UserRole.USER)
        );

        assertThat(publicId).isNotBlank();
        assertThat(passwordHashCaptor.getValue()).isNotEqualTo("secret123");
        assertThat(passwordEncoder.matches("secret123", passwordHashCaptor.getValue())).isTrue();
    }
}
