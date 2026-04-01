package com.dochiri.authservice.infrastructure.repository;

import com.dochiri.authservice.domain.AuthUser;
import com.dochiri.authservice.infrastructure.AuthUserEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

    public AuthUser toDomain(AuthUserEntity entity) {
        return new AuthUser(
                entity.getUserId(),
                entity.getPublicId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getRole()
        );
    }

    public AuthUserEntity toEntity(AuthUser authUser) {
        return AuthUserEntity.from(
                authUser.userId(),
                authUser.publicId(),
                authUser.email(),
                authUser.passwordHash(),
                authUser.role()
        );
    }

    public void apply(AuthUser authUser, AuthUserEntity entity) {
        entity.update(authUser.passwordHash(), authUser.role());
    }
}
