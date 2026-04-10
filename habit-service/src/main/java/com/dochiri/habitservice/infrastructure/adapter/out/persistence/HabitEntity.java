package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.HabitType;
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
                @UniqueConstraint(name = "uk_owner_public_id", columnNames = {"owner_type", "owner_reference_id", "public_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HabitEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String publicId;

    @Column(nullable = false, length = 20)
    private String ownerType;

    @Column(nullable = false)
    private String ownerReferenceId;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HabitType type;

    public static HabitEntity create(String publicId, String ownerType, String ownerReferenceId, String name, HabitType type) {
        HabitEntity entity = new HabitEntity();
        entity.publicId = requireNonNull(publicId);
        entity.ownerType = requireNonNull(ownerType);
        entity.ownerReferenceId = requireNonNull(ownerReferenceId);
        entity.name = requireNonNull(name);
        entity.type = requireNonNull(type);
        return entity;
    }

}