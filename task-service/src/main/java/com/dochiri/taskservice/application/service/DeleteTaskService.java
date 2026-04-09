package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.DeleteTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.DeleteTaskCommand;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteTaskService implements DeleteTaskUseCase {

    private final TaskRepository taskRepository;
    private final TaskOwnerGuard taskOwnerGuard;

    @Transactional
    @Override
    public void delete(DeleteTaskCommand command) {
        Task task = taskRepository.loadById(command.taskId());
        taskOwnerGuard.validateUserOwner(task, command.requesterUserId());
        taskRepository.delete(task);
    }
}
