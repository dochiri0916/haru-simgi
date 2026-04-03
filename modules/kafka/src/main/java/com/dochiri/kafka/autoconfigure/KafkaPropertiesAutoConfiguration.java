package com.dochiri.kafka.autoconfigure;

import com.dochiri.kafka.properties.KafkaErrorHandlerProperties;
import com.dochiri.kafka.properties.KafkaTopicProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({
        KafkaTopicProperties.class,
        KafkaErrorHandlerProperties.class
})
class KafkaPropertiesAutoConfiguration {
}