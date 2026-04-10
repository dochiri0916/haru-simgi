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

    @Transactional
    @Override
    public TaskSummaryResult reopen(ReopenTaskCommand command) {
        Task task = taskRepository.loadById(command.id());

        task.validateOwnership(command.requesterUserId());

        task.reopen();

        Task saved = taskRepository.save(task);

        return new TaskSummaryResult(
                saved.getId(),
                saved.getOwner().type().name(),
                saved.getOwner().referenceId(),
                saved.getTitle().value(),
                saved.isCompleted(),
                saved.getCompletedAt(),
                saved.getDueDate()
        );
    }

}