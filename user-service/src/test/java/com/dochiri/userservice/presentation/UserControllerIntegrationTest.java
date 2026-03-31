package com.dochiri.userservice.presentation;

import com.dochiri.userservice.infrastructure.user.UserEntity;
import com.dochiri.userservice.infrastructure.user.UserJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
    void registerUpsertsExistingUserByEmail() throws Exception {
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.publicId").value(originalPublicId));

        UserEntity updatedUser = userJpaRepository.findByEmail("alice@example.com").orElseThrow();
        assertThat(userJpaRepository.count()).isEqualTo(1);
        assertThat(updatedUser.getPublicId()).isEqualTo(originalPublicId);
        assertThat(updatedUser.getPasswordHash()).isNotEqualTo(originalPasswordHash);
        assertThat(passwordEncoder.matches("new-secret456", updatedUser.getPasswordHash())).isTrue();
    }

}
