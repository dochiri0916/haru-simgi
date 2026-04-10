package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitDetailResult;

public interface GetHabitDetailUseCase {

    GetHabitDetailResult execute(GetHabitDetailCommand command);

}
