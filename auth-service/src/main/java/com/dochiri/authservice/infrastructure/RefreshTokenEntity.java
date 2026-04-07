package com.dochiri.authservice.infrastructure;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "refresh_tokens",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_refresh_tokens_token_id", columnNames = "token_id"),
                @UniqueConstraint(name = "uk_refresh_tokens_user_id", columnNames = "user_id")
        },
        indexes = {
                @Index(name = "idx_refresh_tokens_expires_at", columnList = "expires_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String tokenId;

    @Column(nullable = false)
    private Instant expiresAt;

    public static RefreshTokenEntity from(Long userId, String tokenId, Instant expiresAt) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.userId = requireNonNull(userId);
        entity.tokenId = requireNonNull(tokenId);
        entity.expiresAt = requireNonNull(expiresAt);
        return entity;
    }

    public void update(String tokenId, Instant expiresAt) {
        this.tokenId = requireNonNull(tokenId);
        this.expiresAt = requireNonNull(expiresAt);
    }

}