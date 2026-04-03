package com.dochiri.kafka.autoconfigure;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;

@Configuration(proxyBeanMethods = false)
class KafkaListenerAutoConfiguration {

    @Bean("kafkaListenerContainerFactory")
    @ConditionalOnBean(ConsumerFactory.class)
    @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
    <K, V> ConcurrentKafkaListenerContainerFactory<K, V> kafkaListenerContainerFactory(
            ConsumerFactory<K, V> consumerFactory,
            ObjectProvider<CommonErrorHandler> commonErrorHandlerProvider
    ) {
        ConcurrentKafkaListenerContainerFactory<K, V> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        commonErrorHandlerProvider.ifAvailable(factory::setCommonErrorHandler);
        return factory;
    }

}