package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;

public interface CreateHabitUseCase {

    CreateHabitResult execute(CreateHabitCommand command);

}