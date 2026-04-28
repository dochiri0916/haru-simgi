package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsCommand;
import com.dochiri.habitservice.application.port.in.dto.MigrateGuestHabitsResult;

public interface MigrateGuestHabitsUseCase {

    MigrateGuestHabitsResult execute(MigrateGuestHabitsCommand command);
}
