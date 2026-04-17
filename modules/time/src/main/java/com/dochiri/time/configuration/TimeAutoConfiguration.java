package com.dochiri.time.configuration;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * 애플리케이션 전역에서 사용할 기본 {@link Clock} 빈을 등록한다.
 */
@AutoConfiguration
@EnableConfigurationProperties(TimeProperties.class)
public class TimeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    Clock clock(TimeProperties properties) {
        return Clock.system(properties.timezone());
    }

}
