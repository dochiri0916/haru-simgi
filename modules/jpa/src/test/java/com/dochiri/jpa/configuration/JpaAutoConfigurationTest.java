package com.dochiri.jpa.configuration;

import org.hibernate.cfg.JdbcSettings;
import org.hibernate.cfg.MappingSettings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.mock.env.MockEnvironment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class JpaAutoConfigurationTest {

    private final JpaAutoConfiguration configuration = new JpaAutoConfiguration();

    @Test
    void fallback_AuditorAware는_String_시스템_사용자_ID를_반환한다() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("security.system-user-id", "7");

        AuditorAware<String> auditorAware = configuration.auditorAware(environment);

        assertThat(auditorAware.getCurrentAuditor()).contains("7");
    }

    @Test
    void 감사_시간은_Clock의_Instant를_그대로_사용한다() {
        Instant now = Instant.parse("2026-04-17T14:07:57.248810Z");
        Clock clock = Clock.fixed(now, ZoneId.of("Asia/Seoul"));
        ObjectProvider<Clock> clockProvider = new ObjectProvider<>() {
            @Override
            public Clock getIfAvailable(Supplier<Clock> defaultSupplier) {
                return clock;
            }
        };

        DateTimeProvider provider = configuration.jpaAuditingDateTimeProvider(clockProvider);

        assertThat(provider.getNow()).contains(now);
    }

    @Test
    void Hibernate_JDBC_시간대는_Asia_Seoul을_기본값으로_사용한다() {
        HibernatePropertiesCustomizer customizer = configuration.jpaHibernatePropertiesCustomizer();
        Map<String, Object> hibernateProperties = new HashMap<>();

        customizer.customize(hibernateProperties);

        assertThat(hibernateProperties).containsEntry(JdbcSettings.JDBC_TIME_ZONE, "Asia/Seoul");
        assertThat(hibernateProperties).containsEntry(MappingSettings.PREFERRED_INSTANT_JDBC_TYPE, "TIMESTAMP");
    }

    @Test
    void Hibernate_시간_저장_정책이_이미_있으면_덮어쓰지_않는다() {
        HibernatePropertiesCustomizer customizer = configuration.jpaHibernatePropertiesCustomizer();
        Map<String, Object> hibernateProperties = new HashMap<>();
        hibernateProperties.put(JdbcSettings.JDBC_TIME_ZONE, "UTC");
        hibernateProperties.put(MappingSettings.PREFERRED_INSTANT_JDBC_TYPE, "TIMESTAMP_UTC");

        customizer.customize(hibernateProperties);

        assertThat(hibernateProperties).containsEntry(JdbcSettings.JDBC_TIME_ZONE, "UTC");
        assertThat(hibernateProperties).containsEntry(MappingSettings.PREFERRED_INSTANT_JDBC_TYPE, "TIMESTAMP_UTC");
    }
}
