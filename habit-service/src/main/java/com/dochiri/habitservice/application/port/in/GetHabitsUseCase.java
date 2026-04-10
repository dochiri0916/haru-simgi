package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.GetHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitsResult;

public interface GetHabitsUseCase {

    GetHabitsResult execute(GetHabitsCommand command);

}
