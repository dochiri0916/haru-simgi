package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitRecordsUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitRecordsResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitId;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitDuration;
import com.dochiri.habitservice.domain.record.HabitRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetHabitRecordsService implements GetHabitRecordsUseCase {

    private final HabitRepository habitRepository;
    private final HabitRecordRepository habitRecordRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public GetHabitRecordsResult execute(GetHabitRecordsCommand command) {

        HabitId habitId = HabitId.of(command.habitId());
        HabitOwner owner = HabitOwner.user(command.ownerPublicId());

        Habit habit = habitRepository.loadById(habitId);

        habit.assertOwner(owner);

        List<HabitRecord> records = findRecords(command, habitId);
        List<GetHabitRecordsResult.RecordDto> recordDtos = records.stream()
                .map(this::toDto)
                .toList();

        return new GetHabitRecordsResult(
                habitId.value(),
                recordDtos
        );
    }

    private List<HabitRecord> findRecords(GetHabitRecordsCommand command, HabitId habitId) {
        if (command.fromDate() == null && command.toDate() == null) {
            return habitRecordRepository.findByHabitId(habitId);
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate fromDate = command.fromDate() != null ? command.fromDate() : today.minusMonths(1);
        LocalDate toDate = command.toDate() != null ? command.toDate() : today;
        Instant fromDateTime = fromDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toDateTime = toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        return habitRecordRepository.findByHabitIdAndCompletedAtBetween(
                habitId,
                fromDateTime,
                toDateTime
        );
    }

    private GetHabitRecordsResult.RecordDto toDto(HabitRecord record) {
        Integer minutes = minutesOf(record);

        return new GetHabitRecordsResult.RecordDto(
                record.getId().value(),
                record.getCompletedAt(),
                minutes != null ? minutes : 0,
                calculateLevel(minutes),
                record.getMemo().value()
        );
    }

    private Integer minutesOf(HabitRecord record) {
        return Optional.ofNullable(record.getDuration())
                .map(HabitDuration::minutes)
                .orElse(null);
    }

    private int calculateLevel(Integer minutes) {
        return Math.max(1, GrassLevelPolicy.calculate(minutes != null ? minutes : 0).getLevel());
    }

}
