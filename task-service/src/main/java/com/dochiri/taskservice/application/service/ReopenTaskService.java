package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.ReopenTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.ReopenTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReopenTaskService implements ReopenTaskUseCase {

    private final TaskRepository taskRepository;
    private final TaskOwnerGuard taskOwnerGuard;

    @Transactional
    @Override
    public TaskSummaryResult reopen(ReopenTaskCommand command) {
        Task task = taskRepository.loadById(command.taskId());
        taskOwnerGuard.validateUserOwner(task, command.requesterUserId());
        task.reopen();
        return TaskSummaryResult.from(taskRepository.save(task));
    }
}
