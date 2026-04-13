package com.dochiri.authservice.infrastructure.adapter.out.persistence;

import com.dochiri.authservice.domain.AuthAccount;
import com.dochiri.authservice.domain.AuthProvider;
import com.dochiri.security.role.UserRole;
import org.springframework.stereotype.Component;

@Component
public class AuthAccountMapper {

    public AuthAccount toDomain(AuthAccountEntity entity) {
        return new AuthAccount(
                entity.getUserId(),
                entity.getPublicId(),
                AuthProvider.valueOf(entity.getProvider()),
                entity.getProviderUserId(),
                entity.getPasswordHash(),
                UserRole.from(entity.getRole())
        );
    }

    public AuthAccountEntity toEntity(AuthAccount authAccount) {
        return AuthAccountEntity.from(
                authAccount.userId(),
                authAccount.publicId(),
                authAccount.provider(),
                authAccount.providerUserId(),
                authAccount.passwordHash(),
                authAccount.role()
        );
    }

    public void apply(AuthAccount authAccount, AuthAccountEntity entity) {
        entity.update(
                authAccount.passwordHash(),
                authAccount.role()
        );
    }

}
