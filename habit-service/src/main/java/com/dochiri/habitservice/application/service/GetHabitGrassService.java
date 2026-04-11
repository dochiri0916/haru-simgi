package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.domain.GrassLevel;
import com.dochiri.habitservice.domain.HabitOwner;
import com.dochiri.habitservice.domain.HabitRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetHabitGrassService implements GetHabitGrassUseCase {

    private final HabitRecordRepository repository;

    @Override
    public GetHabitGrassResult execute(GetHabitGrassCommand command) {
        Instant fromInstant = command.fromDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant toInstant = command.toDate().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

        List<HabitRecord> records = repository.findByOwnerBetweenDates(
                HabitOwner.user(command.ownerReferenceId()),
                fromInstant,
                toInstant
        );

        Map<LocalDate, Integer> valueCounts = new HashMap<>();
        int totalValue = 0;
        ZoneId zoneId = ZoneId.systemDefault();
        for (HabitRecord record : records) {
            LocalDate date = record.getCompletedAt().atZone(zoneId).toLocalDate();
            valueCounts.merge(date, record.getValue(), Integer::sum);
            totalValue += record.getValue();
        }

        List<GetHabitGrassResult.HabitGrassDayResult> days = initializeDaysWithValues(
                command.fromDate(),
                command.toDate(),
                valueCounts
        );

        return new GetHabitGrassResult(
                command.fromDate(),
                command.toDate(),
                totalValue,
                days
        );
    }

    private List<GetHabitGrassResult.HabitGrassDayResult> initializeDaysWithValues(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> valueCounts
    ) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    int value = valueCounts.getOrDefault(date, 0);
                    int level = GrassLevel.from(value).level();
                    return new GetHabitGrassResult.HabitGrassDayResult(date, value, level);
                })
                .toList();
    }

}