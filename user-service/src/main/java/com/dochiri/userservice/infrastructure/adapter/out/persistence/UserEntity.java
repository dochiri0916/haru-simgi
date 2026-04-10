package com.dochiri.userservice.infrastructure.adapter.out.persistence;

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

    @Column(nullable = false, length = 36)
    private String publicId;

    @Column(length = 100)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    public static UserEntity from(String publicId, String nickname, String profileImageUrl) {
        UserEntity entity = new UserEntity();
        entity.publicId = requireNonNull(publicId);
        entity.nickname = nickname;
        entity.profileImageUrl = profileImageUrl;
        return entity;
    }

}
