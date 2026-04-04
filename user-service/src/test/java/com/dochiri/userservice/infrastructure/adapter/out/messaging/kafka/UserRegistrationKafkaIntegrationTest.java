package com.dochiri.userservice.infrastructure.adapter.out.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dochiri.userservice.application.port.in.RegisterUserUseCase;
import com.dochiri.userservice.application.port.in.dto.RegisterUserCommand;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:user-kafka-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
@EmbeddedKafka(partitions = 1, topics = "user.registered")
class UserRegistrationKafkaIntegrationTest {

    private static final String TOPIC = "user.registered";

    @Autowired
    private RegisterUserUseCase registerUserUseCase;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private ObjectMapper objectMapper;

    private Consumer<String, String> consumer;

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void 회원가입하면_카프카로_회원가입_이벤트를_발행한다() {
        consumer = createConsumer();

        registerUserUseCase.register(new RegisterUserCommand("alice@example.com", "secret123"));

        ConsumerRecord<String, String> record =
                KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(10));
        UserRegisteredMessage message = readMessage(record.value());

        assertThat(record.key()).isNotBlank();
        assertThat(message.email()).isEqualTo("alice@example.com");
        assertThat(message.password()).isEqualTo("secret123");
        assertThat(message.role()).isEqualTo("USER");
    }

    private Consumer<String, String> createConsumer() {
        Map<String, Object> properties = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ConsumerConfig.GROUP_ID_CONFIG, "user-service-it",
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true",
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        Consumer<String, String> newConsumer = new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new StringDeserializer()
        ).createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(newConsumer, TOPIC);
        return newConsumer;
    }

    private UserRegisteredMessage readMessage(String payload) {
        try {
            return objectMapper.readValue(payload, UserRegisteredMessage.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Kafka 메시지 역직렬화에 실패했습니다.", exception);
        }
    }
}
