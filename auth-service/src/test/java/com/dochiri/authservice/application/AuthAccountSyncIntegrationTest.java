package com.dochiri.authservice.application;

import com.dochiri.authservice.application.port.in.AuthenticateUseCase;
import com.dochiri.authservice.application.port.in.ChangeUserRoleUseCase;
import com.dochiri.authservice.application.port.in.ReissueTokenUseCase;
import com.dochiri.authservice.application.port.in.SyncAuthUserUseCase;
import com.dochiri.authservice.application.port.in.dto.ChangeUserRoleCommand;
import com.dochiri.authservice.application.port.in.dto.LoginCommand;
import com.dochiri.authservice.application.port.in.dto.RefreshTokenCommand;
import com.dochiri.authservice.application.port.in.dto.SyncAuthUserCommand;
import com.dochiri.errorhandling.BaseException;
import com.dochiri.security.jwt.JwtProvider;
import com.dochiri.security.role.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:auth-sync-it;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class AuthAccountSyncIntegrationTest {

    @Autowired
    private SyncAuthUserUseCase syncAuthUserUseCase;

    @Autowired
    private AuthenticateUseCase authenticateUseCase;

    @Autowired
    private ChangeUserRoleUseCase changeUserRoleUseCase;

    @Autowired
    private ReissueTokenUseCase reissueTokenUseCase;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 동일_사용자를_다시_동기화하면_이메일과_비밀번호가_갱신된다() {
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                UserRole.USER
        ));
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice+updated@example.com",
                passwordEncoder.encode("changed-secret"),
                UserRole.USER
        ));

        assertThatThrownBy(() -> authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123")))
                .isInstanceOf(BaseException.class);

        var result = authenticateUseCase.authenticate(new LoginCommand("alice+updated@example.com", "changed-secret"));

        assertThat(result.accessToken()).isNotBlank();
        assertThat(result.refreshToken()).isNotBlank();
    }

    @Test
    void 새로_로그인하면_이전_리프레시_토큰은_더이상_사용할_수_없다() {
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                UserRole.USER
        ));

        var first = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));
        var second = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));

        assertThat(first.refreshToken()).isNotEqualTo(second.refreshToken());
        assertThatThrownBy(() -> reissueTokenUseCase.reissue(new RefreshTokenCommand(first.refreshToken())))
                .isInstanceOf(BaseException.class);
        assertThat(reissueTokenUseCase.reissue(new RefreshTokenCommand(second.refreshToken())).accessToken())
                .isNotBlank();
    }

    @Test
    void 권한을_변경하면_기존_리프레시_토큰은_무효화되고_다시_로그인한_토큰에_새_role이_반영된다() {
        syncAuthUserUseCase.sync(new SyncAuthUserCommand(
                1L,
                "user-public-id",
                "alice@example.com",
                passwordEncoder.encode("secret123"),
                UserRole.USER
        ));

        var firstLogin = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));

        changeUserRoleUseCase.changeRole(new ChangeUserRoleCommand(1L, UserRole.ADMIN));

        assertThatThrownBy(() -> reissueTokenUseCase.reissue(new RefreshTokenCommand(firstLogin.refreshToken())))
                .isInstanceOf(BaseException.class);

        var secondLogin = authenticateUseCase.authenticate(new LoginCommand("alice@example.com", "secret123"));
        var claims = jwtProvider.parseAndValidate(secondLogin.accessToken());

        assertThat(secondLogin.role()).isEqualTo(UserRole.ADMIN);
        assertThat(jwtProvider.extractRole(claims)).isEqualTo("ADMIN");
    }
}
