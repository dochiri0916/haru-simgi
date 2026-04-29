package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.CreateHabitRecordUseCase;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordCommand;
import com.dochiri.habitservice.application.port.in.dto.CreateHabitRecordResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.time.Zones;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateHabitRecordService implements CreateHabitRecordUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final Clock clock;

    @Transactional
    @Override
    public CreateHabitRecordResult execute(CreateHabitRecordCommand command) {
        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = command.owner();

        Habit habit = habitRepository.loadById(habitId);
        habit.assertOwner(owner);

        Instant completedAt = command.completedAt() != null ? command.completedAt() : Instant.now(clock);
        LocalDate completedDate = completedAt.atZone(Zones.DATABASE).toLocalDate();

        return habitRecordRepository.findByHabitIdAndCompletedDate(habitId, completedDate)
                .map(CreateHabitRecordResult::from)
                .orElseGet(() -> createRecord(command, habitId, completedAt));
    }

    private CreateHabitRecordResult createRecord(
            CreateHabitRecordCommand command,
            HabitId habitId,
            Instant completedAt
    ) {
        HabitRecord record = HabitRecord.create(habitId, completedAt, command.minutes(), command.memo());
        HabitRecord saved = habitRecordRepository.save(record);

        return CreateHabitRecordResult.from(saved);
    }

}
