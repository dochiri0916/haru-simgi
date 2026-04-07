package com.dochiri.configserver;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.profiles.active=native",
        "spring.cloud.config.server.native.search-locations=classpath:/test-config",
        "security.username=test-admin",
        "security.password=test-password"
})
class SecurityConfigTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void given_커스텀_Config_서버_자격증명이_주어지면_when_헬스체크에_기본인증을_요청할_때_then_설정값으로만_인증된다() throws Exception {
        HttpResponse<String> successResponse = sendHealthRequest("test-admin", "test-password");
        HttpResponse<String> failureResponse = sendHealthRequest("adbin", "adbin");

        assertThat(successResponse.statusCode()).isEqualTo(200);
        assertThat(successResponse.body()).contains("\"status\":\"UP\"");
        assertThat(failureResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void given_인증정보가_없으면_when_헬스체크를_호출할_때_then_세션없이_401을_반환한다() throws Exception {
        HttpResponse<String> response = sendHealthRequest(null, null);

        assertThat(response.statusCode()).isEqualTo(401);
        assertThat(response.headers().firstValue("Set-Cookie")).isEmpty();
    }

    private HttpResponse<String> sendHealthRequest(String username, String password) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/actuator/health"))
                .GET();

        if (username != null && password != null) {
            String credentials = Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));

            requestBuilder.header("Authorization", "Basic " + credentials);
        }

        return httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }

}