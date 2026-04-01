package com.dochiri.authservice.presentation;

import com.dochiri.authservice.application.port.in.RegisterUserUseCase;
import com.dochiri.authservice.application.port.in.dto.RegisterUserCommand;
import com.dochiri.authservice.application.port.in.dto.RegisterUserResult;
import com.dochiri.authservice.infrastructure.user.UserEntity;
import com.dochiri.authservice.infrastructure.user.UserJpaRepository;
import com.dochiri.errorhandling.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        userJpaRepository.deleteAll();
    }

    @Test
    void registerCreatesUserWhenEmailDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "alice@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.publicId").isNotEmpty());

        UserEntity savedUser = userJpaRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(savedUser.getPublicId()).isNotBlank();
        assertThat(passwordEncoder.matches("secret123", savedUser.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "alice@example.com",
                                "password", "secret123"
                        ))))
                .andExpect(status().isOk());

        UserEntity originalUser = userJpaRepository.findByEmail("alice@example.com").orElseThrow();
        String originalPublicId = originalUser.getPublicId();
        String originalPasswordHash = originalUser.getPasswordHash();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "alice@example.com",
                                "password", "new-secret456"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("이미 가입된 이메일입니다."))
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"))
                .andExpect(jsonPath("$.email").value("alice@example.com"));

        UserEntity updatedUser = userJpaRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(userJpaRepository.count()).isEqualTo(1);
        assertThat(updatedUser.getPublicId()).isEqualTo(originalPublicId);
        assertThat(updatedUser.getPasswordHash()).isEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("secret123", updatedUser.getPasswordHash())).isTrue();
    }

    @Test
    void registerRejectsConcurrentRequestsForSameEmail() throws Exception {
        int requestCount = 8;
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch readyLatch = new CountDownLatch(requestCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(requestCount);
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        List<RegisterUserResult> successes = Collections.synchronizedList(new ArrayList<>());

        try {
            for (int index = 0; index < requestCount; index++) {
                String password = "secret-" + index;

                executorService.submit(() -> {
                    readyLatch.countDown();
                    try {
                        startLatch.await();
                        successes.add(registerUserUseCase.register(new RegisterUserCommand("alice@example.com", password)));
                    } catch (Throwable throwable) {
                        failures.add(throwable);
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            assertThat(readyLatch.await(5, TimeUnit.SECONDS)).isTrue();
            startLatch.countDown();
            assertThat(doneLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } finally {
            executorService.shutdownNow();
        }

        assertThat(successes).hasSize(1);
        assertThat(failures).hasSize(requestCount - 1);
        assertThat(failures).allMatch(BaseException.class::isInstance);
        assertThat(userJpaRepository.count()).isEqualTo(1);

        UserEntity savedUser = userJpaRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(savedUser.getPublicId()).isNotBlank();
        RegisterUserResult successfulResult = successes.getFirst();
        assertThat(savedUser.getPublicId()).isEqualTo(successfulResult.publicId());
    }
}
