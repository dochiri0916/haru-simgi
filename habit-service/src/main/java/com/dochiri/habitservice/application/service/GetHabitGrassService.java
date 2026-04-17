package com.dochiri.habitservice.application.service;

import com.dochiri.habitservice.application.port.in.GetHabitGrassUseCase;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassCommand;
import com.dochiri.habitservice.application.port.in.dto.GetHabitGrassResult;
import com.dochiri.habitservice.application.port.out.HabitGrassAggregation;
import com.dochiri.habitservice.application.port.out.HabitRecordRepository;
import com.dochiri.habitservice.application.port.out.HabitRepository;
import com.dochiri.habitservice.domain.habit.Habit;
import com.dochiri.habitservice.domain.habit.HabitOwner;
import com.dochiri.habitservice.domain.grass.GrassLevelPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

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

        Map<LocalDate, HabitGrassAggregation> grassByDate = habitRecordRepository
                .aggregateGrassByOwnerAndCompletedDateBetween(owner, fromDate, toDate);

        int totalValue = grassByDate.values()
                .stream()
                .mapToInt(HabitGrassAggregation::totalMinutes)
                .sum();

        List<GetHabitGrassResult.HabitGrassDayResult> days =
                initializeDays(fromDate, toDate, grassByDate);

        return new GetHabitGrassResult(
                fromDate,
                toDate,
                totalValue,
                days
        );
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
            Map<LocalDate, HabitGrassAggregation> grassByDate
    ) {
        return from.datesUntil(to.plusDays(1))
                .map(date -> {
                    HabitGrassAggregation aggregation = grassByDate.getOrDefault(date, new HabitGrassAggregation(0, 0));
                    int value = aggregation.totalMinutes();

                    int level = calculateLevel(value, aggregation.completedCount());

                    return new GetHabitGrassResult.HabitGrassDayResult(
                            date,
                            value,
                            level
                    );
                })
                .toList();
    }

    private int calculateLevel(int totalMinutes, int completedCount) {
        int level = GrassLevelPolicy.calculate(totalMinutes).getLevel();

        if (completedCount > 0) {
            return Math.max(1, level);
        }

        return level;
    }

}
