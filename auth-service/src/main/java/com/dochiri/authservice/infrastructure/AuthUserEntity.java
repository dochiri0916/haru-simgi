package com.dochiri.authservice.infrastructure;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "auth_users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuthUserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String publicId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;

    public static AuthUserEntity from(Long userId, String publicId, String email, String passwordHash, String role) {
        AuthUserEntity entity = new AuthUserEntity();
        entity.userId = requireNonNull(userId);
        entity.publicId = requireNonNull(publicId);
        entity.email = requireNonNull(email);
        entity.passwordHash = requireNonNull(passwordHash);
        entity.role = requireNonNull(role);
        return entity;
    }

    public void update(String passwordHash, String role) {
        this.passwordHash = requireNonNull(passwordHash);
        this.role = requireNonNull(role);
    }
}
