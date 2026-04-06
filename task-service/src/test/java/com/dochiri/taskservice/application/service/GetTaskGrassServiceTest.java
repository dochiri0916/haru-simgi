package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.port.in.dto.GetTaskGrassCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTaskGrassServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final Clock clock = Clock.system(ZoneId.of("Asia/Seoul"));

    private GetTaskGrassService getTaskGrassService;

    @BeforeEach
    void setUp() {
        getTaskGrassService = new GetTaskGrassService(taskRepository, clock);
    }

    @Test
    void 완료된_할일을_일자별_잔디_데이터로_집계한다() {
        TaskOwner owner = TaskOwner.user("user-1");
        when(taskRepository.findCompletedByOwnerBetween(
                owner,
                Instant.parse("2026-04-01T15:00:00Z"),
                Instant.parse("2026-04-04T15:00:00Z")
        )).thenReturn(List.of(
                Task.from("task-1", owner, "운동", true, Instant.parse("2026-04-01T23:00:00Z")),
                Task.from("task-2", owner, "독서", true, Instant.parse("2026-04-02T01:00:00Z")),
                Task.from("task-3", owner, "산책", true, Instant.parse("2026-04-03T10:00:00Z"))
        ));

        TaskGrassResult result = getTaskGrassService.getGrass(
                new GetTaskGrassCommand(owner, LocalDate.of(2026, 4, 2), LocalDate.of(2026, 4, 4))
        );

        assertThat(result.totalCompletedCount()).isEqualTo(3);
        assertThat(result.days()).hasSize(3);
        assertThat(result.days().get(0).date()).isEqualTo(LocalDate.of(2026, 4, 2));
        assertThat(result.days().get(0).completedCount()).isEqualTo(2);
        assertThat(result.days().get(1).date()).isEqualTo(LocalDate.of(2026, 4, 3));
        assertThat(result.days().get(1).completedCount()).isEqualTo(1);
        assertThat(result.days().get(2).date()).isEqualTo(LocalDate.of(2026, 4, 4));
        assertThat(result.days().get(2).completedCount()).isZero();
    }

    @Test
    void 시작일이_종료일보다_늦으면_예외가_발생한다() {
        assertThatThrownBy(() -> getTaskGrassService.getGrass(
                new GetTaskGrassCommand(
                        TaskOwner.user("user-1"),
                        LocalDate.of(2026, 4, 5),
                        LocalDate.of(2026, 4, 4)
                )
        )).isInstanceOf(BaseException.class);
    }
}
