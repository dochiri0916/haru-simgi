package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.Nickname;
import com.dochiri.userservice.domain.ProfileImageUrl;
import com.dochiri.userservice.domain.User;
import com.dochiri.userservice.domain.UserId;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreateUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreateUserService createUserService = new CreateUserService(userRepository);

    @Test
    void 소셜_사용자를_생성한다() {
        when(userRepository.findByIdempotencyKey("KAKAO:100"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class), eq("KAKAO:100")))
                .thenAnswer(invocation -> {
                    User input = invocation.getArgument(0);
                    return User.from(
                            1L,
                            UserId.of(input.getId().value()),
                            input.getNickname(),
                            input.getProfileImageUrl()
                    );
                });

        var result = createUserService.execute(new CreateUserCommand(
                "KAKAO:100",
                Nickname.of("alice"),
                ProfileImageUrl.of("https://example.com/alice.png")
        ));

        assertThat(result.publicId()).isNotBlank();
        assertThat(result.nickname()).isEqualTo("alice");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/alice.png");
        verify(userRepository).save(any(User.class), eq("KAKAO:100"));
    }
}
