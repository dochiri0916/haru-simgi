package com.dochiri.time.configuration;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TimePropertiesTest {

    @Test
    void timezone이_null이면_예외가_발생한다() {
        assertThatNullPointerException()
                .isThrownBy(() -> new TimeProperties(null));
    }

    @Test
    void timezone을_명시하면_해당_ZoneId가_사용된다() {
        TimeProperties properties = new TimeProperties(ZoneId.of("UTC"));

        assertThat(properties.timezone()).isEqualTo(ZoneId.of("UTC"));
    }
}
