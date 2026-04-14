package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.dochiri.habitservice.domain.habit.OwnerType.*;
import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit.QHabitEntity.habitEntity;
import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.record.QHabitRecordEntity.habitRecordEntity;

@RequiredArgsConstructor
public class HabitRecordRepositoryCustomImpl implements HabitRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            String ownerType,
            String ownerPublicId,
            Instant fromDate,
            Instant toDate
    ) {
        return queryFactory
                .selectFrom(habitRecordEntity)
                .where(
                        habitRecordEntity.habitId.in(
                                        queryFactory
                                                .select(habitEntity.publicId)
                                                .from(habitEntity)
                                                .where(
                                                        habitEntity.ownerType.eq(valueOf(ownerType))
                                                                .and(habitEntity.ownerPublicId.eq(ownerPublicId))
                                                )
                                )
                                .and(habitRecordEntity.completedAt.between(fromDate, toDate))
                )
                .fetch();
    }

}
