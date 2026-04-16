package com.dochiri.habitservice.application.port.in;

import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexCommand;
import com.dochiri.habitservice.application.port.in.dto.SwapHabitIndexResult;

public interface SwapHabitIndexUseCase {

    SwapHabitIndexResult execute(SwapHabitIndexCommand command);

}
