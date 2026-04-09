package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.port.in.dto.ReopenTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReopenTaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final TaskOwnerGuard taskOwnerGuard = new TaskOwnerGuard();

    private ReopenTaskService reopenTaskService;

    @BeforeEach
    void setUp() {
        reopenTaskService = new ReopenTaskService(taskRepository, taskOwnerGuard);
    }

    @Test
    void 완료된_할일을_다시_미완료로_되돌린다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", true, Instant.parse("2026-04-06T12:34:56Z"));
        Task reopenedTask = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", false, null);

        when(taskRepository.loadById("task-1")).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(reopenedTask);

        TaskSummaryResult result = reopenTaskService.reopen(new ReopenTaskCommand("task-1", "user-1"));

        assertThat(result.id()).isEqualTo("task-1");
        assertThat(result.completed()).isFalse();
        assertThat(result.completedAt()).isNull();
    }

    @Test
    void 다른_사용자_소유의_할일은_다시_열수_없다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", true, Instant.parse("2026-04-06T12:34:56Z"));

        when(taskRepository.loadById("task-1")).thenReturn(task);

        assertThatThrownBy(() -> reopenTaskService.reopen(new ReopenTaskCommand("task-1", "user-2")))
                .isInstanceOf(BaseException.class);
    }
}
