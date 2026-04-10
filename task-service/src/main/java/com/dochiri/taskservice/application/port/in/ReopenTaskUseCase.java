package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.ReopenTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskSummaryResult;

public interface ReopenTaskUseCase {

    TaskSummaryResult reopen(ReopenTaskCommand command);

}