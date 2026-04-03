package com.dochiri.kafka.properties;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaErrorHandlerPropertiesTest {

    @Test
    void 에러핸들러_기본값을_적용한다() {
        KafkaErrorHandlerProperties properties =
                new KafkaErrorHandlerProperties(null, null, null, null, null);

        assertThat(properties.enabled()).isTrue();
        assertThat(properties.retryIntervalMs()).isEqualTo(1000L);
        assertThat(properties.maxAttempts()).isEqualTo(3L);
        assertThat(properties.ackAfterHandle()).isFalse();
        assertThat(properties.dlq().enabled()).isFalse();
        assertThat(properties.dlq().suffix()).isEqualTo(".DLQ");
    }
}
