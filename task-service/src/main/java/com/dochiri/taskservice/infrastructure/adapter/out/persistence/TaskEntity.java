package com.dochiri.taskservice.infrastructure.adapter.out.persistence;

import com.dochiri.jpa.entity.BaseEntity;
import com.dochiri.taskservice.domain.OwnerType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OwnerType ownerType;

    @Column(nullable = false)
    private String ownerReferenceId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private boolean completed;

    @Column
    private Instant completedAt;

    public static TaskEntity from(
            String publicId,
            OwnerType ownerType,
            String ownerReferenceId,
            String title,
            boolean completed,
            Instant completedAt
    ) {
        TaskEntity entity = new TaskEntity();
        entity.publicId = requireNonNull(publicId);
        entity.ownerType = requireNonNull(ownerType);
        entity.ownerReferenceId = requireNonNull(ownerReferenceId);
        entity.title = requireNonNull(title);
        entity.completed = completed;
        entity.completedAt = completedAt;
        return entity;
    }

    public void updateFrom(
            OwnerType ownerType,
            String ownerReferenceId,
            String title,
            boolean completed,
            Instant completedAt
    ) {
        this.ownerType = requireNonNull(ownerType);
        this.ownerReferenceId = requireNonNull(ownerReferenceId);
        this.title = requireNonNull(title);
        this.completed = completed;
        this.completedAt = completedAt;
    }
}
