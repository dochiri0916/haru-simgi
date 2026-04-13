package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateHabitRecordService implements CreateHabitRecordUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Transactional
    @Override
    public CreateHabitRecordResult execute(CreateHabitRecordCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());

        Habit habit = habitRepository.loadById(habitId);
        habit.assertOwner(owner);

        HabitCompletion completion = toCompletion(command);

        HabitRecord record = habit.complete(completion);
        HabitRecord saved = habitRecordRepository.save(record);

        return toResult(saved);
    }

    private HabitCompletion toCompletion(CreateHabitRecordCommand command) {
        if (command.minutes() == null) {
            return HabitCompletion.withoutDuration(command.completedAt());
        }

        return HabitCompletion.withDuration(
                command.completedAt(),
                HabitDuration.of(command.minutes())
        );
    }

    private CreateHabitRecordResult toResult(HabitRecord saved) {
        return new CreateHabitRecordResult(
                saved.getId().value(),
                saved.getHabitId().value(),
                saved.getCompletedAt(),
                saved.hasDuration() ? saved.getDuration().minutes() : null
        );
    }

}