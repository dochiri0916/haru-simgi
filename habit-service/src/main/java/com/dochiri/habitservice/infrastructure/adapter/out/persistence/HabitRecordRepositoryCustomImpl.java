package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;

import static com.dochiri.habitservice.domain.OwnerType.*;
import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.QHabitEntity.habitEntity;
import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.QHabitRecordEntity.habitRecordEntity;

@RequiredArgsConstructor
public class HabitRecordRepositoryCustomImpl implements HabitRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            String ownerType,
            String ownerReferenceId,
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
                                                                .and(habitEntity.ownerReferenceId.eq(ownerReferenceId))
                                                )
                                )
                                .and(habitRecordEntity.completedAt.between(fromDate, toDate))
                )
                .fetch();
    }

}