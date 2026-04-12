package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.OwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabitJpaRepository extends JpaRepository<HabitEntity, Long> {

    Optional<HabitEntity> findByPublicId(String publicId);

    @Query("SELECT h FROM HabitEntity h WHERE h.ownerType = :ownerType AND h.ownerReferenceId = :ownerReferenceId")
    List<HabitEntity> findByOwnerTypeAndOwnerReferenceId(@Param("ownerType") OwnerType ownerType, @Param("ownerReferenceId") String ownerReferenceId);

}