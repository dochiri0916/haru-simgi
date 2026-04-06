package com.dochiri.taskservice.infrastructure.adapter.in.web.external.response;

import com.dochiri.taskservice.application.port.in.dto.TaskGrassDayResult;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassResult;

import java.time.LocalDate;
import java.util.List;

public record TaskGrassResponse(
        LocalDate from,
        LocalDate to,
        int totalCompletedCount,
        List<TaskGrassDayResponse> days
) {
    public static TaskGrassResponse from(TaskGrassResult result) {
        return new TaskGrassResponse(
                result.from(),
                result.to(),
                result.totalCompletedCount(),
                result.days().stream()
                        .map(TaskGrassDayResponse::from)
                        .toList()
        );
    }

    public record TaskGrassDayResponse(
            LocalDate date,
            int completedCount,
            int level
    ) {
        static TaskGrassDayResponse from(TaskGrassDayResult result) {
            return new TaskGrassDayResponse(result.date(), result.completedCount(), toLevel(result.completedCount()));
        }

        private static int toLevel(int completedCount) {
            if (completedCount <= 0) {
                return 0;
            }
            if (completedCount == 1) {
                return 1;
            }
            if (completedCount == 2) {
                return 2;
            }
            if (completedCount <= 4) {
                return 3;
            }
            return 4;
        }
    }
}
