package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.DeleteTaskCommand;

public interface DeleteTaskUseCase {

    void delete(DeleteTaskCommand command);

}