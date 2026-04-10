package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;

public interface CreateHabitRecordUseCase {

    CreateHabitRecordResult execute(CreateHabitRecordCommand command);

}