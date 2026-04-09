package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.jpa.entity.BaseEntity;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Entity
@Table(
        name = "auth_users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auth_users_provider_user_id", columnNames = {"provider", "providerUserId"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAccountEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private Long userId;

    @Column(nullable = false, updatable = false, length = 20)
    private String provider;

    @Column(length = 100)
    private String providerUserId;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    public static AuthAccountEntity from(
            Long userId,
            AuthProvider provider,
            String providerUserId,
            String passwordHash,
            UserRole role
    ) {
        AuthAccountEntity entity = new AuthAccountEntity();
        entity.userId = requireNonNull(userId);
        entity.provider = requireNonNull(provider).name();
        entity.providerUserId = providerUserId;
        entity.passwordHash = requireNonNull(passwordHash);
        entity.role = requireNonNull(role).name();
        return entity;
    }

    public void update(String passwordHash, UserRole role) {
        this.passwordHash = requireNonNull(passwordHash);
        this.role = requireNonNull(role).name();
    }

}
