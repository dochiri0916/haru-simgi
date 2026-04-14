package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "habit_completions",
        indexes = {
                @Index(name = "idx_completed_at", columnList = "completed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_habit_completion_date",
                        columnNames = {"habit_id", "completed_at"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HabitRecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String publicId;

    @Column(nullable = false, length = 36)
    private String habitId;

    @Column(nullable = false)
    private Instant completedAt;

    @Column(nullable = false)
    private boolean completed;

    @Column
    private Integer durationMinutes;

    public HabitRecordEntity(
            String publicId,
            String habitId,
            Instant completedAt,
            boolean completed,
            Integer durationMinutes
    ) {
        this.publicId = requireNonNull(publicId);
        this.habitId = requireNonNull(habitId);
        this.completedAt = requireNonNull(completedAt);
        this.completed = completed;
        this.durationMinutes = durationMinutes;
    }

}