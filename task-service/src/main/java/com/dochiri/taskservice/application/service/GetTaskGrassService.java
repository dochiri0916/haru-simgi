package com.dochiri.taskservice.application.service;

import com.dochiri.errorhandling.BaseException;
import com.dochiri.taskservice.application.error.TaskErrorCode;
import com.dochiri.taskservice.application.port.in.GetTaskGrassUseCase;
import com.dochiri.taskservice.application.port.in.dto.GetTaskGrassCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassDayResult;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassResult;
import com.dochiri.taskservice.application.port.out.TaskRepository;
import com.dochiri.taskservice.domain.Task;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetTaskGrassService implements GetTaskGrassUseCase {

    private final TaskRepository taskRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public TaskGrassResult getGrass(GetTaskGrassCommand command) {
        validateRange(command.from(), command.to());

        ZoneId zoneId = clock.getZone();
        Instant fromInclusive = command.from().atStartOfDay(zoneId).toInstant();
        Instant toExclusive = command.to().plusDays(1).atStartOfDay(zoneId).toInstant();

        List<Task> tasks = taskRepository.findCompletedByOwnerBetween(command.owner(), fromInclusive, toExclusive);
        Map<LocalDate, Integer> countsByDate = initializeCounts(command.from(), command.to());

        for (Task task : tasks) {
            LocalDate date = task.getCompletedAt().atZone(zoneId).toLocalDate();
            countsByDate.computeIfPresent(date, (ignored, count) -> count + 1);
        }

        List<TaskGrassDayResult> days = countsByDate.entrySet().stream()
                .map(entry -> new TaskGrassDayResult(entry.getKey(), entry.getValue()))
                .toList();

        return new TaskGrassResult(
                command.from(),
                command.to(),
                tasks.size(),
                days
        );
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new BaseException(TaskErrorCode.TASK_GRASS_INVALID_DATE_RANGE);
        }
    }

    private Map<LocalDate, Integer> initializeCounts(LocalDate from, LocalDate to) {
        Map<LocalDate, Integer> countsByDate = new LinkedHashMap<>();
        LocalDate current = from;

        while (!current.isAfter(to)) {
            countsByDate.put(current, 0);
            current = current.plusDays(1);
        }

        return countsByDate;
    }
}
