package com.dochiri.habitservice.infrastructure.adapter.out.persistence;

import com.dochiri.habitservice.domain.OwnerType;
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
                        columnNames = {"owner_type", "owner_reference_id", "public_id"}
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

    @Enumerated(EnumType.STRING) // 🔥 핵심
    @Column(nullable = false, length = 20)
    private OwnerType ownerType;

    @Column(nullable = false)
    private String ownerReferenceId;

    @Column(nullable = false, length = 50)
    private String name;

    public HabitEntity(
            String publicId,
            OwnerType ownerType,
            String ownerReferenceId,
            String name
    ) {
        this.publicId = requireNonNull(publicId);
        this.ownerType = requireNonNull(ownerType);
        this.ownerReferenceId = requireNonNull(ownerReferenceId);
        this.name = requireNonNull(name);
    }

    public void changeName(String name) {
        this.name = requireNonNull(name);
    }

}