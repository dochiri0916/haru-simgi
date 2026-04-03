package com.dochiri.kafka.autoconfigure;

import com.dochiri.kafka.properties.KafkaErrorHandlerProperties;
import com.dochiri.kafka.properties.KafkaTopicProperties;
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
