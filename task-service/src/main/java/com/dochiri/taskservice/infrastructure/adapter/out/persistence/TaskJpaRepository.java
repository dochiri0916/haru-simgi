package com.dochiri.taskservice.infrastructure.adapter.out.persistence;

import com.dochiri.taskservice.domain.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByPublicId(String publicId);

    List<TaskEntity> findAllByOwnerTypeAndOwnerReferenceIdOrderByCreatedAtDesc(OwnerType ownerType, String ownerReferenceId);

    List<TaskEntity> findAllByOwnerTypeAndOwnerReferenceIdAndCompletedOrderByCreatedAtDesc(
            OwnerType ownerType,
            String ownerReferenceId,
            boolean completed
    );

    List<TaskEntity> findAllByOwnerTypeAndOwnerReferenceIdAndCompletedIsTrueAndCompletedAtGreaterThanEqualAndCompletedAtLessThan(
            OwnerType ownerType,
            String ownerReferenceId,
            Instant fromInclusive,
            Instant toExclusive
    );
}
