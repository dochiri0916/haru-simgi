package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreateTaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);

    private CreateTaskService createTaskService;

    @BeforeEach
    void setUp() {
        createTaskService = new CreateTaskService(taskRepository);
    }

    @Test
    void 할일_생성에_성공한다() {
        CreateTaskCommand command = new CreateTaskCommand(TaskOwner.guest("guest-1"), "오늘 운동하기");
        Task savedTask = Task.from("task-public-id", TaskOwner.guest("guest-1"), "오늘 운동하기", false);

        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        CreateTaskResult result = createTaskService.create(command);

        assertThat(result.id()).isEqualTo("task-public-id");
        assertThat(result.ownerType()).isEqualTo("GUEST");
        assertThat(result.ownerReferenceId()).isEqualTo("guest-1");
        assertThat(result.title()).isEqualTo("오늘 운동하기");
        assertThat(result.completed()).isFalse();
    }
}
