package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.habitservice.domain.habit.OwnerType;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit.QHabitEntity.habitEntity;
import static com.dochiri.habitservice.infrastructure.adapter.out.persistence.record.QHabitRecordEntity.habitRecordEntity;

@RequiredArgsConstructor
public class HabitRecordRepositoryCustomImpl implements HabitRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<HabitRecordEntity> findCompletionsForOwnerBetweenDates(
            OwnerType ownerType,
            String ownerPublicId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return queryFactory
                .selectFrom(habitRecordEntity)
                .where(
                        habitRecordEntity.habitId.in(
                                        queryFactory
                                                .select(habitEntity.publicId)
                                                .from(habitEntity)
                                                .where(
                                                        habitEntity.ownerType.eq(ownerType)
                                                                .and(habitEntity.ownerPublicId.eq(ownerPublicId))
                                                )
                                )
                                .and(habitRecordEntity.completedDate.between(fromDate, toDate))
                )
                .fetch();
    }

}
