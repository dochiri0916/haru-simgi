package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitJpaRepository extends JpaRepository<HabitEntity, Long> {

    Optional<HabitEntity> findByPublicId(String publicId);

    List<HabitEntity> findByOwnerTypeAndOwnerReferenceId(String ownerType, String ownerReferenceId);

}