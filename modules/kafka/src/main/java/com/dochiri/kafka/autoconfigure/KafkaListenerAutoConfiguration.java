package com.dochiri.kafka.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.CommonErrorHandler;

@Configuration(proxyBeanMethods = false)
class KafkaListenerAutoConfiguration {

    @Bean
    @ConditionalOnBean(CommonErrorHandler.class)
    KafkaListenerContainerFactoryCustomizer kafkaListenerContainerFactoryCustomizer(CommonErrorHandler commonErrorHandler) {
        return factory -> factory.setCommonErrorHandler(commonErrorHandler);
    }

    @Bean
    @ConditionalOnBean(KafkaListenerContainerFactoryCustomizer.class)
    KafkaListenerFactoryPostProcessor kafkaListenerFactoryPostProcessor(KafkaListenerContainerFactoryCustomizer customizer) {
        return new KafkaListenerFactoryPostProcessor(customizer);
    }
}
