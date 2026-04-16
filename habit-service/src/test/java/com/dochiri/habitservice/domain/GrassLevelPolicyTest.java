package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrassLevelPolicyTest {

    @Test
    void 완료_기록_수로_잔디_레벨을_계산한다() {
        assertThat(GrassLevelPolicy.calculate(0).getLevel()).isZero();
        assertThat(GrassLevelPolicy.calculate(1).getLevel()).isEqualTo(1);
        assertThat(GrassLevelPolicy.calculate(2).getLevel()).isEqualTo(2);
        assertThat(GrassLevelPolicy.calculate(3).getLevel()).isEqualTo(3);
        assertThat(GrassLevelPolicy.calculate(4).getLevel()).isEqualTo(3);
        assertThat(GrassLevelPolicy.calculate(5).getLevel()).isEqualTo(4);
    }

}
