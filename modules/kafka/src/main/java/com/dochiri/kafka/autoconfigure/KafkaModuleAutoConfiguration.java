package com.dochiri.kafka.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        KafkaTopicAutoConfiguration.class,
        KafkaErrorHandlingAutoConfiguration.class,
        KafkaListenerAutoConfiguration.class,
        KafkaPropertiesAutoConfiguration.class
})
public class KafkaModuleAutoConfiguration {
}