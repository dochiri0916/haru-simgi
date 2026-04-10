package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.GetTasksCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;

import java.util.List;

public interface GetTasksUseCase {

    List<TaskSummaryResult> getTasks(GetTasksCommand command);

}