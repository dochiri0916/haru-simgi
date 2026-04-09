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
class SecurityConfigurationTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Test
    void Config_서버_자격증명으로_헬스체크_인증을_검증한다() throws Exception {

        // given
        String correctUsername = "test-admin";
        String correctPassword = "test-password";
        String wrongUsername = "adbin";
        String wrongPassword = "adbin";

        // when
        HttpResponse<String> successResponse = sendHealthRequest(correctUsername, correctPassword);
        HttpResponse<String> failureResponse = sendHealthRequest(wrongUsername, wrongPassword);

        // then
        assertThat(successResponse.statusCode()).isEqualTo(200);
        assertThat(successResponse.body()).contains("\"status\":\"UP\"");
        assertThat(failureResponse.statusCode()).isEqualTo(401);
    }

    @Test
    void 인증정보없이_헬스체크_호출시_401을_반환한다() throws Exception {

        // given
        // 인증 정보 없음

        // when
        HttpResponse<String> response = sendHealthRequest(null, null);

        // then
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