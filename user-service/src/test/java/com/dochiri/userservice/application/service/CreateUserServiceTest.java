package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreateUserService createUserService = new CreateUserService(userRepository);

    @Test
    void 소셜_사용자를_생성한다() {
        when(userRepository.save(any(User.class)))
                .thenReturn(1L);

        var result = createUserService.execute(new CreateUserCommand(
                "alice",
                "https://example.com/alice.png"
        ));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("alice");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/alice.png");
        verify(userRepository).save(any(User.class));
    }
}
