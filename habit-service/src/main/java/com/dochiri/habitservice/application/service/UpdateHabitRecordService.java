package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.UpdateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.UpdateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.record.HabitRecordId;
import com.dochiri.habitservice.domain.record.exception.HabitRecordNotFoundException;
import org.openapitools.jackson.nullable.JsonNullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UpdateHabitRecordService implements UpdateHabitRecordUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Transactional
    @Override
    public UpdateHabitRecordResult execute(UpdateHabitRecordCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitRecordId recordId = HabitRecordId.of(command.recordId());
        HabitOwner owner = command.owner();

        Habit habit = habitRepository.loadById(habitId);
        habit.assertOwner(owner);

        HabitRecord record = habitRecordRepository.loadById(recordId);
        if (!record.getHabitId().equals(habitId)) {
            throw new HabitRecordNotFoundException(recordId);
        }

        Instant completedAt = command.completedAt() != null
                ? command.completedAt()
                : record.getCompletedAt();
        Integer minutes = command.minutes() != null
                ? command.minutes()
                : record.hasDuration() ? record.getDuration().minutes() : null;
        String memo = JsonNullable.undefined().equals(command.memo())
                ? record.getMemo().value()
                : command.memo().orElse(null);

        HabitRecord updated = record.update(completedAt, minutes, memo);
        HabitRecord saved = habitRecordRepository.save(updated);

        return UpdateHabitRecordResult.from(saved);
    }

}
