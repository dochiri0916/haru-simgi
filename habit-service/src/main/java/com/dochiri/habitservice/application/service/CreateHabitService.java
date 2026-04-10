package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateHabitService implements CreateHabitUseCase {

    private final HabitRepository habitRepository;

    @Override
    public CreateHabitResult execute(CreateHabitCommand command) {
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());
        HabitType type = HabitType.valueOf(command.habitType());

        Habit saved = habitRepository.save(Habit.create(owner, command.name(), type));

        return new CreateHabitResult(
                saved.getId().value(),
                saved.getName().value(),
                saved.getType()
        );
    }

}
