package com.dochiri.userservice.infrastructure.user;

import com.dochiri.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String publicId;

    @Column(nullable = false, updatable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    public static UserEntity from(String publicId, String email, String passwordHash) {
        UserEntity entity = new UserEntity();
        entity.publicId = requireNonNull(publicId);
        entity.email = requireNonNull(email);
        entity.passwordHash = requireNonNull(passwordHash);
        return entity;
    }

    public void updatePasswordHash(String passwordHash) {
        this.passwordHash = requireNonNull(passwordHash);
    }

}