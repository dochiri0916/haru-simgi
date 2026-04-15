package com.dochiri.habitservice.domain;

import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrassLevelPolicyTest {

    @Test
    void 분_기준_상한을_포함해서_잔디_레벨을_계산한다() {
        assertThat(GrassLevelPolicy.calculate(0).getLevel()).isZero();
        assertThat(GrassLevelPolicy.calculate(1).getLevel()).isEqualTo(1);
        assertThat(GrassLevelPolicy.calculate(30).getLevel()).isEqualTo(1);
        assertThat(GrassLevelPolicy.calculate(31).getLevel()).isEqualTo(2);
        assertThat(GrassLevelPolicy.calculate(60).getLevel()).isEqualTo(2);
        assertThat(GrassLevelPolicy.calculate(61).getLevel()).isEqualTo(3);
        assertThat(GrassLevelPolicy.calculate(120).getLevel()).isEqualTo(3);
        assertThat(GrassLevelPolicy.calculate(121).getLevel()).isEqualTo(4);
    }

}
