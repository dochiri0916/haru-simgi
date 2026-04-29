package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.jpa.entity.BaseEntity;
import com.dochiri.time.Zones;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

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
                        columnNames = {"habit_id", "completed_date"}
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
    private LocalDate completedDate;

    @Column
    private Integer durationMinutes;

    @Column(length = 200)
    private String memo;

    public HabitRecordEntity(
            String publicId,
            String habitId,
            Instant completedAt,
            Integer durationMinutes,
            String memo
    ) {
        this.publicId = requireNonNull(publicId);
        this.habitId = requireNonNull(habitId);
        this.completedAt = requireNonNull(completedAt);
        this.completedDate = toDatabaseDate(completedAt);
        this.durationMinutes = durationMinutes;
        this.memo = memo;
    }

    public void update(Instant completedAt, Integer durationMinutes, String memo) {
        this.completedAt = requireNonNull(completedAt);
        this.completedDate = toDatabaseDate(completedAt);
        this.durationMinutes = durationMinutes;
        this.memo = memo;
    }

    private static LocalDate toDatabaseDate(Instant completedAt) {
        return completedAt.atZone(Zones.DATABASE).toLocalDate();
    }

}
