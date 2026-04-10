package com.dochiri.taskservice.application.service;

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
        Task task = taskRepository.loadById(command.id());

        task.validateOwnership(command.requesterUserId());

        Instant completedAt = Instant.now(clock);

        task.complete(completedAt);
        Task saved = taskRepository.save(task);

        return new CompleteTaskResult(
                saved.getId(),
                saved.getOwner().type().name(),
                saved.getOwner().referenceId(),
                saved.getTitle().value(),
                saved.isCompleted(),
                saved.getCompletedAt()
        );
    }

}