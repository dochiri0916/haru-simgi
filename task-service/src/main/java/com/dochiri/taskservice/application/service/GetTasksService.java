package com.dochiri.taskservice.application.service;

import com.dochiri.taskservice.application.port.in.GetTasksUseCase;
import com.dochiri.taskservice.application.port.in.dto.GetTasksCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetTasksService implements GetTasksUseCase {

    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    @Override
    public List<TaskSummaryResult> getTasks(GetTasksCommand command) {
        if (command.completed() == null) {
            return taskRepository.findAllByOwner(command.owner()).stream()
                    .map(TaskSummaryResult::from)
                    .toList();
        }

        return taskRepository.findAllByOwnerAndCompleted(command.owner(), command.completed()).stream()
                .map(TaskSummaryResult::from)
                .toList();
    }
}
