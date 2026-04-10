package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import com.dochiri.taskservice.application.port.in.CreateTaskUseCase;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateTaskService implements CreateTaskUseCase {

    private final TaskRepository taskRepository;
    private final Clock clock;

    @Transactional
    @Override
    public CreateTaskResult create(CreateTaskCommand command) {
        validateDueDate(command.dueDate());

        Task saved = taskRepository.save(
                Task.create(
                        command.owner(),
                        command.title(),
                        command.dueDate()
                )
        );

        return new CreateTaskResult(
                saved.getId(),
                saved.getOwner().type().name(),
                saved.getOwner().referenceId(),
                saved.getTitle().value(),
                saved.isCompleted(),
                saved.getDueDate()
        );
    }

    private void validateDueDate(Instant dueDate) {
        Instant todayStart = LocalDate.now(clock).atStartOfDay(clock.getZone()).toInstant();
        if (dueDate.isBefore(todayStart)) {
            throw new BaseException(TaskErrorCode.TASK_DUE_DATE_IN_PAST);
        }
    }

}