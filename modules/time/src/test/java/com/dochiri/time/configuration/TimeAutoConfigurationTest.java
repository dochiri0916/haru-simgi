package com.dochiri.time.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class TimeAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    PropertyPlaceholderAutoConfiguration.class,
                    TimeAutoConfiguration.class
            ));

    @Test
    void 기본_설정으로_Clock_빈이_Asia_Seoul_타임존으로_등록된다() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(Clock.class);
            Clock clock = context.getBean(Clock.class);
            assertThat(clock.getZone()).isEqualTo(ZoneId.of("Asia/Seoul"));
        });
    }

    @Test
    void timezone_프로퍼티를_지정하면_해당_타임존의_Clock이_등록된다() {
        contextRunner
                .withPropertyValues("time.timezone=UTC")
                .run(context -> {
                    Clock clock = context.getBean(Clock.class);
                    assertThat(clock.getZone()).isEqualTo(ZoneId.of("UTC"));
                });
    }

    @Test
    void TimeProperties_빈이_등록된다() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(TimeProperties.class);
        });
    }

    @Test
    void 사용자가_Clock_빈을_등록하면_기본_Clock은_등록되지_않는다() {
        contextRunner
                .withUserConfiguration(CustomClockConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(Clock.class);
                    assertThat(context.getBean(Clock.class).instant())
                            .isEqualTo(Instant.parse("2026-04-17T00:00:00Z"));
                });
    }

    @Test
    void 잘못된_timezone_프로퍼티면_컨텍스트_기동에_실패한다() {
        contextRunner
                .withPropertyValues("time.timezone=Foo/Bar")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasStackTraceContaining("time.timezone")
                            .hasRootCauseMessage("Unknown time-zone ID: Foo/Bar");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomClockConfiguration {

        @Bean
        Clock customClock() {
            return Clock.fixed(Instant.parse("2026-04-17T00:00:00Z"), ZoneId.of("UTC"));
        }
    }
}
