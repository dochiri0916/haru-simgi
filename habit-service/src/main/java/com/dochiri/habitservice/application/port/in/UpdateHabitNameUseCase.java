package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;

public interface UpdateHabitNameUseCase {

    UpdateHabitNameResult execute(UpdateHabitNameCommand command);

}