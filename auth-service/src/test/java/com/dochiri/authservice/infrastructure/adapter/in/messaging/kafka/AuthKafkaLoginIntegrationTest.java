package com.dochiri.authservice.infrastructure.adapter.in.messaging.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.dto.AuthTokenResult;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:auth-kafka-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=auth-service-it",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
@EmbeddedKafka(partitions = 1, topics = "user.registered")
class AuthKafkaLoginIntegrationTest {

    private static final String TOPIC = "user.registered";

    @Autowired
    private AuthenticateUseCase authenticateUseCase;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
        kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class
        )));

        kafkaListenerEndpointRegistry.getListenerContainers().forEach(container ->
                ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic())
        );
    }

    @AfterEach
    void tearDown() {
        if (kafkaTemplate != null) {
            kafkaTemplate.destroy();
        }
    }

    @Test
    void 회원가입_이벤트를_처리한_뒤_로그인에_성공한다() throws Exception {
        kafkaTemplate.send(TOPIC, "user-public-id", objectMapper.writeValueAsString(new UserRegisteredMessage(
                1L,
                "user-public-id",
                "alice@example.com",
                "secret123",
                "USER"
        ))).get();

        AuthTokenResult result = awaitAuthentication("alice@example.com", "secret123");

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
        assertThat(result.refreshTokenExpiresAt()).isAfter(Instant.now().minusSeconds(1));
    }

    private AuthTokenResult awaitAuthentication(String email, String password) throws InterruptedException {
        Instant deadline = Instant.now().plusSeconds(10);

        while (Instant.now().isBefore(deadline)) {
            try {
                return authenticateUseCase.authenticate(new LoginCommand(email, password));
            } catch (RuntimeException exception) {
                Thread.sleep(Duration.ofMillis(200));
            }
        }

        return authenticateUseCase.authenticate(new LoginCommand(email, password));
    }
}
