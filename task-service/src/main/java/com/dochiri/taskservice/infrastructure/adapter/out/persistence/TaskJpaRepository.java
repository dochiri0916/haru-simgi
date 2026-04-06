package com.dochiri.taskservice.infrastructure.adapter.out.persistence;

import com.dochiri.taskservice.domain.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {

    Optional<TaskEntity> findByPublicId(String publicId);

    List<TaskEntity> findAllByOwnerTypeAndOwnerReferenceId(OwnerType ownerType, String ownerReferenceId);
}
