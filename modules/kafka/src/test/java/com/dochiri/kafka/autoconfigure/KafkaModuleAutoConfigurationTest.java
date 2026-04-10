package com.dochiri.kafka.autoconfigure;

import com.dochiri.kafka.properties.KafkaErrorHandlerProperties;
import com.dochiri.kafka.properties.KafkaTopicProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class KafkaModuleAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(KafkaModuleAutoConfiguration.class));

    @Test
    void 카프카_토픽_프로퍼티가_있으면_NewTopics_빈을_등록한다() {
        contextRunner
                .withUserConfiguration(KafkaAdminConfig.class)
                .withPropertyValues(
                        "dochiri.kafka.topics[0].name=user.created",
                        "dochiri.kafka.topics[0].partitions=3",
                        "dochiri.kafka.topics[0].replicas=1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(KafkaTopicProperties.class);
                    assertThat(context).hasSingleBean(KafkaAdmin.NewTopics.class);

                    KafkaTopicProperties properties = context.getBean(KafkaTopicProperties.class);
                    assertThat(properties.topics())
                            .singleElement()
                            .extracting(KafkaTopicProperties.TopicProperties::name,
                                    KafkaTopicProperties.TopicProperties::partitions,
                                    KafkaTopicProperties.TopicProperties::replicas)
                            .containsExactly("user.created", 3, (short) 1);
                });
    }

    @Test
    void 카프카_토픽_프로퍼티가_없으면_NewTopics_빈을_등록하지_않는다() {
        contextRunner
                .withUserConfiguration(KafkaAdminConfig.class)
                .run(context -> assertThat(context).doesNotHaveBean(KafkaAdmin.NewTopics.class));
    }

    @Test
    void ConsumerFactory가_있으면_기본_리스너_팩토리와_에러핸들러를_등록한다() {
        contextRunner
                .withUserConfiguration(KafkaConsumerConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(CommonErrorHandler.class);
                    assertThat(context).hasSingleBean(KafkaErrorHandlerProperties.class);
                    assertThat(context).hasBean("kafkaListenerContainerFactory");
                });
    }

    @Test
    void 에러핸들러를_비활성화하면_CommonErrorHandler를_등록하지_않는다() {
        contextRunner
                .withPropertyValues("dochiri.kafka.error-handler.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CommonErrorHandler.class));
    }

    @Test
    void DLQ를_활성화하고_KafkaOperations가_있으면_recoverer를_등록한다() {
        contextRunner
                .withUserConfiguration(KafkaOperationsConfig.class)
                .withPropertyValues("dochiri.kafka.error-handler.dlq.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(DeadLetterPublishingRecoverer.class);
                    assertThat(context).hasSingleBean(CommonErrorHandler.class);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class KafkaAdminConfig {

        @Bean
        KafkaAdmin kafkaAdmin() {
            return new KafkaAdmin(Map.of("bootstrap.servers", "localhost:9092"));
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class KafkaOperationsConfig {

        @Bean
        KafkaOperations<Object, Object> kafkaOperations() {
            return mock(KafkaOperations.class);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class KafkaConsumerConfig {

        @Bean
        ConsumerFactory<Object, Object> consumerFactory() {
            return mock(ConsumerFactory.class);
        }
    }
}
