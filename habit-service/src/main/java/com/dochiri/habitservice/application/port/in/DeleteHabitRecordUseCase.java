package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.DeleteHabitRecordCommand;

public interface DeleteHabitRecordUseCase {

    void execute(DeleteHabitRecordCommand command);

}
