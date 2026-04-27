package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.jpa.entity.BaseEntity;
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
                @UniqueConstraint(name = "uk_auth_users_provider_id", columnNames = {"provider", "providerId"}),
                @UniqueConstraint(name = "uk_auth_users_public_id", columnNames = "publicId")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthAccountEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 36)
    private String publicId;

    @Column(nullable = false, updatable = false, length = 20)
    private String provider;

    @Column(length = 100)
    private String providerId;

    @Column(nullable = false)
    private String role;

    public static AuthAccountEntity from(
            String publicId,
            AuthProvider provider,
            String providerId,
            UserRole role
    ) {
        AuthAccountEntity entity = new AuthAccountEntity();
        entity.publicId = requireNonNull(publicId);
        entity.provider = requireNonNull(provider).name();
        entity.providerId = providerId;
        entity.role = requireNonNull(role).name();
        return entity;
    }

    public void update(UserRole role) {
        this.role = requireNonNull(role).name();
    }

}
