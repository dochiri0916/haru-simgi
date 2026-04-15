package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.DeleteHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.DeleteHabitRecordCommand;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.HabitRecordId;
import com.dochiri.habitservice.domain.record.exception.HabitRecordNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteHabitRecordService implements DeleteHabitRecordUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Transactional
    @Override
    public void execute(DeleteHabitRecordCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitRecordId recordId = HabitRecordId.of(command.recordId());
        HabitOwner owner = HabitOwner.user(command.ownerPublicId());

        Habit habit = habitRepository.loadById(habitId);
        habit.assertOwner(owner);

        HabitRecord record = habitRecordRepository.loadById(recordId);
        if (!record.getHabitId().equals(habitId)) {
            throw new HabitRecordNotFoundException(recordId);
        }

        habitRecordRepository.delete(recordId);
    }

}
