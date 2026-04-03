package com.dochiri.kafka.properties;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KafkaTopicPropertiesTest {

    @Test
    void 토픽_목록이_없으면_빈_리스트를_사용한다() {
        KafkaTopicProperties properties = new KafkaTopicProperties(null);

        assertThat(properties.topics()).isEmpty();
    }

    @Test
    void 토픽_기본값을_적용한다() {
        KafkaTopicProperties.TopicProperties topic = new KafkaTopicProperties.TopicProperties("user.created", null, null);

        assertThat(topic.partitions()).isEqualTo(1);
        assertThat(topic.replicas()).isEqualTo((short) 1);
    }

    @Test
    void 토픽명은_비어있을_수_없다() {
        assertThatThrownBy(() -> new KafkaTopicProperties.TopicProperties(" ", 1, (short) 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Kafka topic name must not be blank.");
    }

    @Test
    void 토픽_리스트는_불변이다() {
        KafkaTopicProperties properties = new KafkaTopicProperties(
                List.of(new KafkaTopicProperties.TopicProperties("user.created", 3, (short) 1))
        );

        assertThatThrownBy(() -> properties.topics().add(new KafkaTopicProperties.TopicProperties("user.deleted", 1, (short) 1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
