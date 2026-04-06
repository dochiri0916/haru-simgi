package com.dochiri.security.properties;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorsPropertiesTest {

    @Test
    void allowedOrigins가_null이면_빈_리스트가_된다() {
        CorsProperties properties = new CorsProperties(null, false);

        assertThat(properties.allowedOrigins()).isEmpty();
        assertThat(properties.allowCredentials()).isFalse();
    }

    @Test
    void 와일드카드_origin도_그대로_보관한다() {
        CorsProperties properties = new CorsProperties(List.of("*"), true);

        assertThat(properties.allowedOrigins()).containsExactly("*");
        assertThat(properties.allowCredentials()).isTrue();
    }

    @Test
    void 여러_origin을_설정할_수_있다() {
        CorsProperties properties = new CorsProperties(
                List.of("https://example.com", "https://api.example.com"),
                true);

        assertThat(properties.allowedOrigins()).hasSize(2);
        assertThat(properties.allowCredentials()).isTrue();
    }
}
