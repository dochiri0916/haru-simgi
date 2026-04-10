package com.dochiri.taskservice.application.port.in;

import com.dochiri.taskservice.application.port.in.dto.GetTaskGrassCommand;
import com.dochiri.taskservice.application.port.in.dto.TaskGrassResult;

public interface GetTaskGrassUseCase {

    TaskGrassResult getGrass(GetTaskGrassCommand command);

}