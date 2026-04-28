package com.dochiri.habitservice.infrastructure.adapter.out.persistence.habit;

import com.dochiri.habitservice.domain.habit.ColorType;
import com.dochiri.habitservice.domain.habit.OwnerType;
import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "habits",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_owner_public_id",
                        columnNames = {"owner_type", "owner_public_id", "public_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HabitEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36, unique = true)
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OwnerType ownerType;

    @Column(nullable = false)
    private String ownerPublicId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ColorType color;

    @Column(name = "sort_index", nullable = false)
    private int index;

    public HabitEntity(
            String publicId,
            OwnerType ownerType,
            String ownerPublicId,
            String name,
            ColorType color,
            int index
    ) {
        this.publicId = requireNonNull(publicId);
        this.ownerType = requireNonNull(ownerType);
        this.ownerPublicId = requireNonNull(ownerPublicId);
        this.name = requireNonNull(name);
        this.color = requireNonNull(color);
        this.index = index;
    }

    public void changeName(String name) {
        this.name = requireNonNull(name);
    }

    public void update(String name, ColorType color, int sortIndex) {
        this.name = requireNonNull(name);
        this.color = requireNonNull(color);
        this.index = sortIndex;
    }

    public void migrateOwner(OwnerType ownerType, String ownerPublicId, int sortIndex) {
        this.ownerType = requireNonNull(ownerType);
        this.ownerPublicId = requireNonNull(ownerPublicId);
        this.index = sortIndex;
    }

}
