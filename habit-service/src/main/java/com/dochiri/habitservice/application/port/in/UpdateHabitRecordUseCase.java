package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordResult;

public interface UpdateHabitRecordUseCase {

    UpdateHabitRecordResult execute(UpdateHabitRecordCommand command);

}
