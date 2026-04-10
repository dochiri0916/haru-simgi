package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.CreateTaskCommand;
import com.dochiri.taskservice.application.port.in.dto.CreateTaskResult;

public interface CreateTaskUseCase {

    CreateTaskResult create(CreateTaskCommand command);

}