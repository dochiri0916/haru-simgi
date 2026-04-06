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
public class AuthAccountEntity extends BaseEntity {

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

    public static AuthAccountEntity from(Long userId, String publicId, String email, String passwordHash, String role) {
        AuthAccountEntity entity = new AuthAccountEntity();
        entity.userId = requireNonNull(userId);
        entity.publicId = requireNonNull(publicId);
        entity.email = requireNonNull(email);
        entity.passwordHash = requireNonNull(passwordHash);
        entity.role = requireNonNull(role);
        return entity;
    }

    public void update(String publicId, String email, String passwordHash, String role) {
        this.publicId = requireNonNull(publicId);
        this.email = requireNonNull(email);
        this.passwordHash = requireNonNull(passwordHash);
        this.role = requireNonNull(role);
    }

}