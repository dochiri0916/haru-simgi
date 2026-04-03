package com.dochiri.kafka.autoconfigure;

import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@FunctionalInterface
public interface KafkaListenerContainerFactoryCustomizer {

    void customize(ConcurrentKafkaListenerContainerFactory<?, ?> factory);

}