package com.dochiri.kafka.autoconfigure;

import com.dochiri.kafka.properties.KafkaErrorHandlerProperties;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration(proxyBeanMethods = false)
class KafkaErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommonErrorHandler.class)
    @ConditionalOnProperty(prefix = "dochiri.kafka.error-handler", name = "enabled", havingValue = "true", matchIfMissing = true)
    CommonErrorHandler kafkaCommonErrorHandler(
            KafkaErrorHandlerProperties properties,
            ObjectProvider<DeadLetterPublishingRecoverer> recovererProvider
    ) {
        FixedBackOff backOff = createBackOff(properties);
        DeadLetterPublishingRecoverer recoverer = recovererProvider.getIfAvailable();

        DefaultErrorHandler errorHandler = recoverer == null
                ? new DefaultErrorHandler(backOff)
                : new DefaultErrorHandler(recoverer, backOff);

        errorHandler.setAckAfterHandle(properties.ackAfterHandle());
        return errorHandler;
    }

    @Bean
    @ConditionalOnBean(KafkaOperations.class)
    @ConditionalOnProperty(prefix = "dochiri.kafka.error-handler.dlq", name = "enabled", havingValue = "true")
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaErrorHandlerProperties properties,
            KafkaOperations<Object, Object> kafkaOperations
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(record.topic() + properties.dlq().suffix(), record.partition())
        );
    }

    private FixedBackOff createBackOff(KafkaErrorHandlerProperties properties) {
        return new FixedBackOff(properties.retryIntervalMs(), properties.maxAttempts() - 1);
    }
}
