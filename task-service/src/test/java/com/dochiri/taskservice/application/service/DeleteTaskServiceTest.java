package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.port.in.dto.DeleteTaskCommand;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import com.dochiri.taskservice.domain.TaskOwner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeleteTaskServiceTest {

    private final TaskRepository taskRepository = mock(TaskRepository.class);
    private final TaskOwnerGuard taskOwnerGuard = new TaskOwnerGuard();

    private DeleteTaskService deleteTaskService;

    @BeforeEach
    void setUp() {
        deleteTaskService = new DeleteTaskService(taskRepository, taskOwnerGuard);
    }

    @Test
    void 본인_소유_할일을_삭제한다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", false, null);
        when(taskRepository.loadById("task-1")).thenReturn(task);

        deleteTaskService.delete(new DeleteTaskCommand("task-1", "user-1"));

        verify(taskRepository).delete(task);
    }

    @Test
    void 다른_사용자_소유의_할일은_삭제할_수_없다() {
        Task task = Task.from("task-1", TaskOwner.user("user-1"), "책 읽기", false, null);
        when(taskRepository.loadById("task-1")).thenReturn(task);

        assertThatThrownBy(() -> deleteTaskService.delete(new DeleteTaskCommand("task-1", "user-2")))
                .isInstanceOf(BaseException.class);
    }
}
