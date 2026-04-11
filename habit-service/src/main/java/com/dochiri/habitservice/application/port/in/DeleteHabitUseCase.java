package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.DeleteHabitCommand;

public interface DeleteHabitUseCase {

    void execute(DeleteHabitCommand command);

}