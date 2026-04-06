package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.CompleteTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CompleteTaskResult;

public interface CompleteTaskUseCase {

    CompleteTaskResult complete(CompleteTaskCommand command);
}
