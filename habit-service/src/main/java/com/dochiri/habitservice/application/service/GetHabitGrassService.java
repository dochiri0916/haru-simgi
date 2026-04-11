package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.domain.HabitDuration;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitRecord;
import com.dochiri.habitservice.domain.GrassLevelPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetHabitGrassService implements GetHabitGrassUseCase {

    private final HabitRecordRepository habitRecordRepository;

    @Transactional(readOnly = true)
    @Override
    public GetHabitGrassResult execute(GetHabitGrassCommand command) {

        ZoneId zoneId = ZoneId.systemDefault();

        HabitOwner owner = HabitOwner.user(command.ownerReferenceId());

        Instant fromInstant = command.fromDate()
                .atStartOfDay(zoneId)
                .toInstant();

        Instant toInstant = command.toDate()
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant();

        List<HabitRecord> records = habitRecordRepository
                .findByOwnerAndCompletedAtBetween(owner, fromInstant, toInstant);

        Map<LocalDate, Integer> durationByDate = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCompletedAt().atZone(zoneId).toLocalDate(),
                        Collectors.summingInt(r ->
                                Optional.ofNullable(r.getDuration())
                                        .map(HabitDuration::minutes)
                                        .orElse(0)
                        )
                ));

        int totalMinutes = durationByDate.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        List<GetHabitGrassResult.HabitGrassDayResult> days =
                initializeDays(command.fromDate(), command.toDate(), durationByDate);

        return new GetHabitGrassResult(
                command.fromDate(),
                command.toDate(),
                totalMinutes,
                days
        );
    }

    private List<GetHabitGrassResult.HabitGrassDayResult> initializeDays(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> durationByDate
    ) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    int minutes = durationByDate.getOrDefault(date, 0);

                    int level = GrassLevelPolicy
                            .calculate(minutes)
                            .getLevel();

                    return new GetHabitGrassResult.HabitGrassDayResult(
                            date,
                            minutes,
                            level
                    );
                })
                .toList();
    }

}