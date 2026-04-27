package com.dochiri.userservice.infrastructure.adapter.out.persistence;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_public_id", columnNames = "publicId"),
                @UniqueConstraint(name = "uk_users_idempotency_key", columnNames = "idempotencyKey")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 36)
    private String publicId;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Column(length = 100)
    private String idempotencyKey;

    public UserEntity(
            String publicId,
            String nickname,
            String profileImageUrl,
            String idempotencyKey
    ) {
        this.publicId = requireNonNull(publicId);
        this.nickname = requireNonNull(nickname);
        this.profileImageUrl = requireNonNull(profileImageUrl);
        this.idempotencyKey = idempotencyKey;
    }

}
