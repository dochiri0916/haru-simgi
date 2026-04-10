package com.dochiri.authservice.infrastructure.adapter.in.web.external;

import com.dochiri.authservice.application.port.in.KakaoLoginUseCase;
import com.dochiri.authservice.application.port.in.LogoutUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.dto.IssueAuthTokenResult;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:auth-controller-transport-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.kakao.frontend-redirect-uri=http://localhost:5173/"
})
@AutoConfigureMockMvc
class AuthControllerTransportTest {

    private static final String AUTH_TRANSPORT_HEADER = "X-Auth-Transport";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KakaoLoginUseCase kakaoLoginUseCase;

    @MockitoBean
    private ReissueTokenUseCase reissueTokenUseCase;

    @MockitoBean
    private LogoutUseCase logoutUseCase;

    @Test
    void 기본_로그인은_쿠키를_발급한다() throws Exception {
        given(kakaoLoginUseCase.login(any())).willReturn(tokenResult());

        mockMvc.perform(post("/api/auth/login/kakao")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "kakao-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().stringValues("Set-Cookie",
                        org.hamcrest.Matchers.iterableWithSize(2)))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void bearer_로그인은_쿠키를_발급하지_않는다() throws Exception {
        given(kakaoLoginUseCase.login(any())).willReturn(tokenResult());

        mockMvc.perform(post("/api/auth/login/kakao")
                        .header(AUTH_TRANSPORT_HEADER, "bearer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "kakao-code"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));
    }

    @Test
    void bearer_재발급은_쿠키를_발급하지_않는다() throws Exception {
        given(reissueTokenUseCase.reissue(any())).willReturn(tokenResult());

        mockMvc.perform(post("/api/auth/refresh")
                        .header(AUTH_TRANSPORT_HEADER, "bearer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Set-Cookie"))
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void bearer_로그아웃은_쿠키_삭제_헤더를_내리지_않는다() throws Exception {
        willDoNothing().given(logoutUseCase).logout(any());

        mockMvc.perform(post("/api/auth/logout")
                        .header(AUTH_TRANSPORT_HEADER, "bearer")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "refreshToken": "refresh-token"
                                }
                                """))
                .andExpect(status().isNoContent())
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void bearer_콜백은_리다이렉트만_하고_쿠키를_발급하지_않는다() throws Exception {
        given(kakaoLoginUseCase.login(any())).willReturn(tokenResult());

        mockMvc.perform(get("/api/auth/login/kakao/callback")
                        .header(AUTH_TRANSPORT_HEADER, "bearer")
                        .param("code", "kakao-code"))
                .andExpect(status().isFound())
                .andExpect(header().string(LOCATION, "http://localhost:5173/"))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    private IssueAuthTokenResult tokenResult() {
        return new IssueAuthTokenResult(
                "access-token",
                "refresh-token",
                Instant.parse("2026-04-09T00:00:00Z"),
                UserRole.USER
        );
    }
}
