package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;

public interface GetHabitRecordsUseCase {

    GetHabitRecordsResult execute(GetHabitRecordsCommand command);

}