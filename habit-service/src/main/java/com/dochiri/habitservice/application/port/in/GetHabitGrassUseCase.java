package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;

public interface GetHabitGrassUseCase {

    GetHabitGrassResult execute(GetHabitGrassCommand command);

}