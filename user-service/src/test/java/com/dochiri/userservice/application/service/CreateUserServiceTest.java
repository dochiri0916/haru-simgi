package com.dochiri.userservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.userservice.application.port.in.dto.CreateUserCommand;
import com.dochiri.userservice.application.port.out.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateUserServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CreateUserService createUserService = new CreateUserService(userRepository);

    @Test
    void 일반_사용자_생성에_성공하면_userId와_이메일을_반환한다() {
        when(userRepository.create(any())).thenReturn(1L);

        var result = createUserService.create(new CreateUserCommand("alice@example.com"));

        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo("alice@example.com");
    }

    @Test
    void 이메일이_중복되면_예외가_발생한다() {
        when(userRepository.create(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> createUserService.create(new CreateUserCommand("alice@example.com")))
                .isInstanceOf(BaseException.class);
    }

}
