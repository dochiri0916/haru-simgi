package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.infrastructure.AuthAccountEntity;
import com.dochiri.security.role.UserRole;
import org.springframework.stereotype.Component;

@Component
public class AuthAccountMapper {

    public AuthAccount toDomain(AuthAccountEntity entity) {
        return new AuthAccount(
                entity.getUserId(),
                entity.getPublicId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                UserRole.from(entity.getRole())
        );
    }

    public AuthAccountEntity toEntity(AuthAccount authAccount) {
        return AuthAccountEntity.from(
                authAccount.userId(),
                authAccount.publicId(),
                authAccount.email(),
                authAccount.passwordHash(),
                authAccount.role()
        );
    }

    public void apply(AuthAccount authAccount, AuthAccountEntity entity) {
        entity.update(
                authAccount.publicId(),
                authAccount.email(),
                authAccount.passwordHash(),
                authAccount.role()
        );
    }

}