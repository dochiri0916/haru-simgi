package com.dochiri.kafka.autoconfigure;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

class KafkaListenerFactoryPostProcessor implements BeanPostProcessor {

    private final KafkaListenerContainerFactoryCustomizer customizer;

    KafkaListenerFactoryPostProcessor(KafkaListenerContainerFactoryCustomizer customizer) {
        this.customizer = customizer;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ConcurrentKafkaListenerContainerFactory<?, ?> factory) {
            customizer.customize(factory);
        }
        return bean;
    }

}