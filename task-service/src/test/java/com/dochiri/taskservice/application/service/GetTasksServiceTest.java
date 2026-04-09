package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.dto.GetTasksCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetTasksServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);

    private GetTasksService getTasksService;

    @BeforeEach
    void setUp() {
        getTasksService = new GetTasksService(taskRepository);
    }

    @Test
    void completed_조건이_없으면_전체_할일을_조회한다() {
        TaskOwner owner = TaskOwner.user("user-1");
        when(taskRepository.findAllByOwner(owner)).thenReturn(List.of(
                Task.from("task-2", owner, "잔디 보기", true, Instant.parse("2026-04-09T01:00:00Z")),
                Task.from("task-1", owner, "투두 만들기", false, null)
        ));

        List<TaskSummaryResult> result = getTasksService.getTasks(new GetTasksCommand(owner, null));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("task-2");
        assertThat(result.get(0).completed()).isTrue();
        assertThat(result.get(1).id()).isEqualTo("task-1");
        assertThat(result.get(1).completed()).isFalse();
    }

    @Test
    void completed_조건이_있으면_상태별로_조회한다() {
        TaskOwner owner = TaskOwner.user("user-1");
        when(taskRepository.findAllByOwnerAndCompleted(owner, false)).thenReturn(List.of(
                Task.from("task-1", owner, "투두 만들기", false, null)
        ));

        List<TaskSummaryResult> result = getTasksService.getTasks(new GetTasksCommand(owner, false));

        assertThat(result).singleElement().satisfies(task -> {
            assertThat(task.id()).isEqualTo("task-1");
            assertThat(task.completed()).isFalse();
        });
    }
}
