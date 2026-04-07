package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import com.dochiri.taskservice.application.port.in.CompleteTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CompleteTaskService implements CompleteTaskUseCase {

    private final TaskRepository taskRepository;
    private final Clock clock;

    @Transactional
    @Override
    public CompleteTaskResult complete(CompleteTaskCommand command) {
        Task task = taskRepository.loadById(command.taskId());
        validateTaskOwner(task, command.requesterUserId());
        Instant completedAt = Instant.now(clock);

        task.complete(completedAt);
        Task saved = taskRepository.save(task);

        return CompleteTaskResult.from(saved);
    }

    private void validateTaskOwner(Task task, String requesterUserId) {
        if (!task.getOwner().isUser()) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_FORBIDDEN);
        }
        if (!task.getOwner().referenceId().equals(requesterUserId)) {
            throw new BaseException(TaskErrorCode.TASK_OWNER_FORBIDDEN);
        }
    }
}
