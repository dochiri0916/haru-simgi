package com.dochiri.jpa.configuration;

import org.hibernate.cfg.JdbcSettings;
import org.hibernate.cfg.MappingSettings;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@AutoConfiguration(
        beforeName = "org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration",
        afterName = "com.dochiri.security.autoconfigure.SecurityAutoConfiguration"
)
@EnableJpaAuditing(dateTimeProviderRef = "jpaAuditingDateTimeProvider")
class JpaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuditorAware.class)
    AuditorAware<String> auditorAware(Environment environment) {
        JpaAuditProperties properties = JpaAuditProperties.from(environment);
        return () -> Optional.of(properties.systemUserId().toString());
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaAuditingDateTimeProvider")
    DateTimeProvider jpaAuditingDateTimeProvider(ObjectProvider<Clock> clockProvider) {
        Clock clock = clockProvider.getIfAvailable(Clock::systemUTC);
        return () -> Optional.of(Instant.now(clock));
    }

    @Bean
    @ConditionalOnMissingBean(name = "jpaHibernatePropertiesCustomizer")
    HibernatePropertiesCustomizer jpaHibernatePropertiesCustomizer() {
        return hibernateProperties -> {
            hibernateProperties.putIfAbsent(JdbcSettings.JDBC_TIME_ZONE, "Asia/Seoul");
            hibernateProperties.putIfAbsent(MappingSettings.PREFERRED_INSTANT_JDBC_TYPE, "TIMESTAMP");
        };
    }

}
