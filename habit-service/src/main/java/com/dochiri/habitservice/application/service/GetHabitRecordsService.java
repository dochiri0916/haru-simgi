package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.Habit;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitRecord;
import com.dochiri.habitservice.domain.exception.HabitNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetHabitRecordsService implements GetHabitRecordsUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;

    @Override
    public GetHabitRecordsResult execute(GetHabitRecordsCommand command) {
        Habit habit = habitRepository.findById(command.habitId())
                .orElseThrow(HabitNotFoundException::new);

        habit.validateOwner(HabitOwner.user(command.ownerReferenceId()));

        List<HabitRecord> records = habitRecordRepository.findByHabitIdBetweenDates(
                command.habitId(),
                command.fromDate(),
                command.toDate()
        );

        List<GetHabitRecordsResult.RecordDto> recordDtos = records.stream()
                .map(r -> new GetHabitRecordsResult.RecordDto(
                        r.getId().value(),
                        r.getCompletedAt(),
                        r.getValue()
                ))
                .toList();

        return new GetHabitRecordsResult(command.habitId(), recordDtos);
    }

}