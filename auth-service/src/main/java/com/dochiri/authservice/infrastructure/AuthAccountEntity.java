package com.dochiri.authservice.infrastructure;

import com.dochiri.jpa.entity.BaseEntity;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @Column(unique = true)
    private String email;

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
            String email,
            String passwordHash,
            UserRole role
    ) {
        AuthAccountEntity entity = new AuthAccountEntity();
        entity.userId = requireNonNull(userId);
        entity.provider = requireNonNull(provider).name();
        entity.providerUserId = providerUserId;
        entity.email = email;
        entity.passwordHash = requireNonNull(passwordHash);
        entity.role = requireNonNull(role).name();
        return entity;
    }

    public void update(String email, String passwordHash, UserRole role) {
        this.email = email;
        this.passwordHash = requireNonNull(passwordHash);
        this.role = requireNonNull(role).name();
    }

}