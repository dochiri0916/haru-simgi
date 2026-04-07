package com.dochiri.userservice.application.service;

import com.dochiri.userservice.application.port.in.dto.ProvisionSocialUserCommand;
import com.dochiri.userservice.application.port.out.UserRepository;
import com.dochiri.userservice.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProvisionSocialUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final ProvisionSocialUserService provisionSocialUserService = new ProvisionSocialUserService(userRepository);

    @Test
    void 이메일이_있고_이미_가입된_경우_기존_사용자_ID를_반환한다() {
        when(userRepository.findIdByEmail("alice@example.com"))
                .thenReturn(Optional.of(1L));

        var result = provisionSocialUserService.provision(new ProvisionSocialUserCommand(
                "alice@example.com",
                "alice",
                "https://example.com/alice.png"
        ));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.nickname()).isEqualTo("alice");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/alice.png");
        verify(userRepository, never()).create(any(User.class));
    }

    @Test
    void 이메일이_있고_처음_로그인한_경우_사용자를_생성한다() {
        when(userRepository.findIdByEmail("new-user@example.com"))
                .thenReturn(Optional.<Long>empty());
        when(userRepository.create(any(User.class)))
                .thenReturn(2L);

        var result = provisionSocialUserService.provision(new ProvisionSocialUserCommand(
                "new-user@example.com",
                "new-user",
                "https://example.com/new-user.png"
        ));

        assertThat(result.userId()).isEqualTo(2L);
        assertThat(result.email()).isEqualTo("new-user@example.com");
        assertThat(result.nickname()).isEqualTo("new-user");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/new-user.png");
        verify(userRepository).create(any(User.class));
    }

    @Test
    void 동시_생성_충돌이_나면_기존_사용자_ID를_다시_조회한다() {
        when(userRepository.findIdByEmail("race@example.com"))
                .thenReturn(Optional.<Long>empty(), Optional.of(3L));
        when(userRepository.create(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        var result = provisionSocialUserService.provision(new ProvisionSocialUserCommand(
                "race@example.com",
                "race",
                "https://example.com/race.png"
        ));

        assertThat(result.userId()).isEqualTo(3L);
        assertThat(result.email()).isEqualTo("race@example.com");
        assertThat(result.nickname()).isEqualTo("race");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/race.png");
    }

    @Test
    void 이메일이_없어도_소셜_사용자를_생성한다() {
        when(userRepository.create(any(User.class)))
                .thenReturn(4L);

        var result = provisionSocialUserService.provision(new ProvisionSocialUserCommand(
                null,
                "social-only",
                "https://example.com/social-only.png"
        ));

        assertThat(result.userId()).isEqualTo(4L);
        assertThat(result.email()).isNull();
        assertThat(result.nickname()).isEqualTo("social-only");
        assertThat(result.profileImageUrl()).isEqualTo("https://example.com/social-only.png");
        verify(userRepository).create(any(User.class));
    }
}
