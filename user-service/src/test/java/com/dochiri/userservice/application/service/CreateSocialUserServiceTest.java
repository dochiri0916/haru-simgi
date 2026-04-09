package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.dto.CreateSocialUserCommand;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateSocialUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreateSocialUserService createSocialUserService = new CreateSocialUserService(userRepository);

    @Test
    void 소셜_사용자를_생성한다() {
        when(userRepository.create(any(User.class)))
                .thenReturn(1L);

        var result = createSocialUserService.create(new CreateSocialUserCommand(
                "alice",
                "https://example.com/alice.png"
        ));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.nickname()).isEqualTo("alice");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/alice.png");
        verify(userRepository).create(any(User.class));
    }
}
