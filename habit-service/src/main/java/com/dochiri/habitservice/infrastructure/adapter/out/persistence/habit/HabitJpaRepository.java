package com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit;

import com.dochiri.habitservice.domain.habit.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitJpaRepository extends JpaRepository<HabitEntity, Long> {

    Optional<HabitEntity> findByPublicId(String publicId);

    List<HabitEntity> findByOwnerTypeAndOwnerPublicIdOrderByIndexAscCreatedAtAsc(
            OwnerType ownerType,
            String ownerPublicId
    );

    Optional<HabitEntity> findTopByOwnerTypeAndOwnerPublicIdOrderByIndexDesc(
            OwnerType ownerType,
            String ownerPublicId
    );

}
