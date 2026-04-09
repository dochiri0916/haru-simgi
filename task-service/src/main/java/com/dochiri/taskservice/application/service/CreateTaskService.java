package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.CreateTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateTaskService implements CreateTaskUseCase {

    private final TaskRepository taskRepository;

    @Transactional
    @Override
    public CreateTaskResult create(CreateTaskCommand command) {
        Task newTask = Task.create(command.owner(), command.title());
        Task saved = taskRepository.save(newTask);

        return new CreateTaskResult(
                saved.getId(),
                saved.getOwner().type().name(),
                saved.getOwner().referenceId(),
                saved.getTitle(),
                saved.isCompleted()
        );
    }

}