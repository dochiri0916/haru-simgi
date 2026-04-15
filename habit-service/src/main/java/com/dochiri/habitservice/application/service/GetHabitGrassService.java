package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.record.HabitRecord;
import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
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
    private final HabitRepository habitRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    @Override
    public GetHabitGrassResult execute(GetHabitGrassCommand command) {

        ZoneId zoneId = clock.getZone();

        HabitOwner owner = HabitOwner.user(command.ownerPublicId());
        LocalDate today = LocalDate.now(clock);
        LocalDate toDate = command.toDate() != null ? command.toDate() : today;
        Optional<LocalDate> firstHabitCreatedDate = firstHabitCreatedDate(owner, zoneId);
        LocalDate requestedFromDate = command.fromDate() != null ? command.fromDate() : firstHabitCreatedDate.orElse(toDate);
        LocalDate fromDate = firstHabitCreatedDate
                .filter(createdDate -> createdDate.isAfter(requestedFromDate))
                .orElse(requestedFromDate);

        Instant fromInstant = fromDate
                .atStartOfDay(zoneId)
                .toInstant();

        Instant toInstant = toDate
                .plusDays(1)
                .atStartOfDay(zoneId)
                .toInstant();

        List<HabitRecord> records = habitRecordRepository
                .findByOwnerAndCompletedAtBetween(owner, fromInstant, toInstant);

        Map<LocalDate, Integer> totalMinutesByDate = records.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getCompletedAt().atZone(zoneId).toLocalDate(),
                        Collectors.summingInt(this::minutesOf)
                ));

        int totalValue = totalMinutesByDate.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        List<GetHabitGrassResult.HabitGrassDayResult> days =
                initializeDays(fromDate, toDate, totalMinutesByDate);

        return new GetHabitGrassResult(
                fromDate,
                toDate,
                totalValue,
                days
        );
    }

    private int minutesOf(HabitRecord record) {
        return record.hasDuration() ? record.getDuration().minutes() : 0;
    }

    private Optional<LocalDate> firstHabitCreatedDate(HabitOwner owner, ZoneId zoneId) {
        return habitRepository.findByOwner(owner)
                .stream()
                .map(Habit::getCreatedAt)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder())
                .map(createdAt -> createdAt.atZone(zoneId).toLocalDate());
    }

    private List<GetHabitGrassResult.HabitGrassDayResult> initializeDays(
            LocalDate from,
            LocalDate to,
            Map<LocalDate, Integer> completionCountByDate
    ) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    int value = completionCountByDate.getOrDefault(date, 0);

                    int level = GrassLevelPolicy
                            .calculate(value)
                            .getLevel();

                    return new GetHabitGrassResult.HabitGrassDayResult(
                            date,
                            value,
                            level
                    );
                })
                .toList();
    }

}
