package com.dochiri.habitservice.infrastructure.adapter.out.persistence.record;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

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

    private static final ZoneId DATABASE_ZONE = ZoneId.of("Asia/Seoul");

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

    @Column(nullable = false)
    private boolean completed;

    @Column
    private Integer durationMinutes;

    @Column(length = 200)
    private String memo;

    public HabitRecordEntity(
            String publicId,
            String habitId,
            Instant completedAt,
            boolean completed,
            Integer durationMinutes,
            String memo
    ) {
        this.publicId = requireNonNull(publicId);
        this.habitId = requireNonNull(habitId);
        this.completedAt = requireNonNull(completedAt);
        this.completedDate = toDatabaseDate(completedAt);
        this.completed = completed;
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
        return completedAt.atZone(DATABASE_ZONE).toLocalDate();
    }

}
