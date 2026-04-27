package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.UpdateHabitNameUseCase;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitNameResult;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitName;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateHabitNameService implements UpdateHabitNameUseCase {

    private final HabitRepository habitRepository;

    @Transactional
    @Override
    public UpdateHabitNameResult execute(UpdateHabitNameCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = command.owner();
        HabitName newName = HabitName.of(command.newName());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        Habit saved = habitRepository.save(habit.rename(newName));

        return UpdateHabitNameResult.from(saved);
    }

}
