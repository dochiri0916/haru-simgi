package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.DeleteHabitUseCase;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitCommand;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteHabitService implements DeleteHabitUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Transactional
    @Override
    public void execute(DeleteHabitCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        habitRecordRepository.deleteByHabitId(habitId);

        habitRepository.delete(habitId);
    }

}