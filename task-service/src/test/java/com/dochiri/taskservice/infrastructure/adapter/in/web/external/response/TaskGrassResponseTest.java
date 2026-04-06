package com.dochiri.taskservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.taskservice.application.port.in.dto.TaskGrassDayResult;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassResult;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TaskGrassResponseTest {

    @Test
    void 완료_건수에_따라_잔디_레벨을_매핑한다() {
        TaskGrassResponse response = TaskGrassResponse.from(
                new TaskGrassResult(
                        LocalDate.of(2026, 4, 1),
                        LocalDate.of(2026, 4, 5),
                        12,
                        List.of(
                                new TaskGrassDayResult(LocalDate.of(2026, 4, 1), 0),
                                new TaskGrassDayResult(LocalDate.of(2026, 4, 2), 1),
                                new TaskGrassDayResult(LocalDate.of(2026, 4, 3), 2),
                                new TaskGrassDayResult(LocalDate.of(2026, 4, 4), 4),
                                new TaskGrassDayResult(LocalDate.of(2026, 4, 5), 5)
                        )
                )
        );

        assertThat(response.days().get(0).level()).isZero();
        assertThat(response.days().get(1).level()).isEqualTo(1);
        assertThat(response.days().get(2).level()).isEqualTo(2);
        assertThat(response.days().get(3).level()).isEqualTo(3);
        assertThat(response.days().get(4).level()).isEqualTo(4);
    }
}
