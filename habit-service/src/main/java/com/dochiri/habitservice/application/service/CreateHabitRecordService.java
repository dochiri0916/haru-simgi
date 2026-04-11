package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitId;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitRecord;
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

        HabitRecord record = habit.createRecord(
                command.completedAt(),
                command.value()
        );

        HabitRecord saved = habitRecordRepository.save(record);

        return new CreateHabitRecordResult(
                saved.getId().value(),
                saved.getHabitId().value(),
                saved.getCompletedAt(),
                saved.getValue().value()
        );
    }

}