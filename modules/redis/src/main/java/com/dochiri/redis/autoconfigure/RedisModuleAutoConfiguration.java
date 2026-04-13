package com.dochiri.redis.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({
        RedisTemplateAutoConfiguration.class
})
public class RedisModuleAutoConfiguration {
}
