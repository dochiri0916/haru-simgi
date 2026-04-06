package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CompleteTaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-04-06T12:34:56Z"), ZoneOffset.UTC);

    private CompleteTaskService completeTaskService;

    @BeforeEach
    void setUp() {
        completeTaskService = new CompleteTaskService(taskRepository, clock);
    }

    @Test
    void 할일_완료에_성공한다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", false, null);
        Task completedTask = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", true, Instant.parse("2026-04-06T12:34:56Z"));

        when(taskRepository.loadById("task-1")).thenReturn(task);
        when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

        CompleteTaskResult result = completeTaskService.complete(new CompleteTaskCommand("task-1", "user-1"));

        assertThat(result.id()).isEqualTo("task-1");
        assertThat(result.completed()).isTrue();
        assertThat(result.completedAt()).isEqualTo(Instant.parse("2026-04-06T12:34:56Z"));
    }

    @Test
    void 다른_사용자_소유의_할일은_완료할_수_없다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", false, null);

        when(taskRepository.loadById("task-1")).thenReturn(task);

        assertThatThrownBy(() -> completeTaskService.complete(new CompleteTaskCommand("task-1", "user-2")))
                .isInstanceOf(BaseException.class);
    }
}
