package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetHabitRecordsService implements GetHabitRecordsUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Transactional(readOnly = true)
    @Override
    public GetHabitRecordsResult execute(GetHabitRecordsCommand command) {

        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        List<HabitRecord> records = habitRecordRepository
                .findByHabitIdAndCompletedAtBetween(
                        habitId,
                        command.fromDate(),
                        command.toDate()
                );

        List<GetHabitRecordsResult.RecordDto> recordDtos = records.stream()
                .map(this::toDto)
                .toList();

        return new GetHabitRecordsResult(
                habitId.value(),
                recordDtos
        );
    }

    private GetHabitRecordsResult.RecordDto toDto(HabitRecord record) {
        return new GetHabitRecordsResult.RecordDto(
                record.getId().value(),
                record.getCompletedAt(),
                Optional.ofNullable(record.getDuration())
                        .map(HabitDuration::minutes)
                        .orElse(null)
        );
    }

}