# 아키텍처 리뷰 — DDD · Hexagonal · MSA

`auth-service`, `user-service`, `habit-service`, 그리고 공용 `modules/`를 대상으로
DDD / 헥사고날 / MSA 관점에서 정합성을 점검한 결과입니다.

---

## 1. 전체 평가 요약

| 관점 | 평가 | 비고 |
|---|---|---|
| 헥사고날 패키지 구조 | 양호 | `domain` / `application(port·service)` / `infrastructure(adapter)` 3-layer가 일관되게 유지됨 |
| 어그리게이트 · VO 설계 | 양호(habit) / 미흡(auth, user) | `Habit`, `HabitRecord`는 모범. `AuthAccount`, `User`는 anemic |
| 포트–어댑터 분리 | 대체로 양호 | 일부 use case가 패턴을 깨뜨림 (KakaoAuthorize, GetCurrentUser, Logout) |
| 트랜잭션 경계 | 양호 | `@Transactional`이 application service에 위치 |
| MSA 경계 | 미흡 | auth-service가 user-service의 PK(`Long userId`)를 직접 저장 |
| 데드 코드 / 일관성 | 다수 발견 | RefreshToken 계열 전체, `findByHabitIdAndCompletedAt` 등 |

---

## 2. DDD 관점

### 2.1 잘 된 점

- **`habit-service` 도메인이 모범적이다.**
  - `HabitId`, `HabitName`, `HabitColor`, `HabitIndex`, `HabitOwner`, `HabitMemo`,
    `HabitDuration`, `HabitCompletion`, `HabitRecordId` — 모두 record 기반 VO로
    compact constructor에서 invariant를 검증한다.
  - `Habit`, `HabitRecord`는 immutable + private constructor + 정적 팩토리
    (`create` / `from`) + 도메인 메서드 (`assertOwner`, `rename`, `reorder`,
    `update`) 패턴을 일관되게 따른다.
  - `HabitRecord`가 `Habit`을 객체 참조가 아닌 `HabitId`로 들고 있어
    어그리게이트 경계가 깔끔하다.
  - `GrassLevelPolicy`를 `@UtilityClass`로 분리한 도메인 서비스 정의도 적절하다.
- **도메인 예외 분리** — 어그리게이트별 디렉토리(`domain/habit/exception`,
  `domain/record/exception`)와 `ErrorCode` enum 매핑이 정리되어 있다.

### 2.2 개선 필요

#### (D-1) `auth-service` / `user-service` 도메인이 anemic하다 — ✅ 수정 완료
- `AuthAccount`, `User`, `RefreshToken`은 단순 데이터 홀더. validate / behavior
  메서드가 없다.
- `AuthAccount`는 `record`인데 compact constructor에서 null/포맷 검증이 없다.
- `User`는 `nickname`, `profileImageUrl`을 raw `String`으로 보유 — 길이/포맷
  invariant를 도메인이 보장하지 못한다 (현재는 `UserEntity`의
  `requireNonNull`과 컬럼 길이에만 의존).
- **권장**: `Nickname`, `ProfileImageUrl`, `ProviderId`, `PublicId`(공통),
  `RefreshTokenId` 등 VO를 도입. `User.changeNickname(...)` 같은 도메인 메서드를
  추가하고, 검증을 도메인으로 끌어올린다.
- **수정 내용**: `Nickname`, `ProfileImageUrl` VO를 user-service 도메인에 도입.
  `User`가 VO를 그대로 보유하고 `changeNickname`, `changeProfileImageUrl` 도메인
  메서드 추가. `AuthAccount`에 compact constructor + `changeRole` 도메인 메서드
  추가 (`RefreshToken`은 데드 코드라 별도 항목에서 제거 권고).

#### (D-2) 도메인에 인프라 누수 — `AuthSession*Exception` — ✅ 수정 완료
- `domain/exception/AuthSessionSerializationException`,
  `AuthSessionDeserializationException`은 Jackson/Redis 직렬화 실패를 표현한다.
  도메인은 “세션을 직렬화한다”는 사실을 알면 안 된다.
- **권장**: 두 예외를 `infrastructure.adapter.out.cache`로 이동. 어댑터에서
  잡아 `BaseException(AUTH_SESSION_SERIALIZATION_FAILED)`로 변환한다.
- **수정 내용**: 두 예외 클래스를
  `infrastructure.adapter.out.cache.exception` 패키지로 이동. 이전 `domain`
  위치의 파일은 삭제. `AuthSessionRedisAdapter` import만 갱신.

#### (D-4) `HabitMemo` null 처리 패턴이 어색함 — ✅ 수정 완료
- `HabitMemo.of(null)`이 `null`을 반환하는 정적 팩토리 — null-Object 또는
  `Optional`로 표현하는 편이 의도가 명확하다.
- `HabitRecord.getMemo()` 호출부마다 `record.getMemo() != null ? ... : null`
  분기가 반복 (`HabitRecordMapper`, `UpdateHabitRecordService`,
  `GetHabitRecordsService`).
- **권장**: `HabitMemo.empty()` 싱글턴 또는 `Optional<HabitMemo>` 노출 후 매퍼
  레이어에서만 null로 환원.
- **수정 내용**: `HabitMemo`에 `EMPTY` 싱글턴 도입. `HabitMemo.of(null)`은
  empty 싱글턴 반환. `HabitRecord.memo`는 항상 non-null. 호출부의
  `!= null` 분기 제거 (`record.getMemo().value()`로 일원화).

#### (D-5) 어그리게이트 명명 — `HabitRecord` vs `HabitCompletion` — ✅ 수정 완료
- DB 테이블은 `habit_completions`, 도메인 record VO는 `HabitCompletion`,
  엔티티/어그리게이트는 `HabitRecord`. 같은 개념을 두 어휘로 부른다.
- **권장**: 한 쪽으로 통일. 도메인 어휘(유비쿼터스 언어)를 “Completion”으로
  맞추는 편이 자연스럽다.
- **수정 내용**: 파라미터 번들 역할이었던 `HabitCompletion` VO를 제거하고
  `HabitRecord.create(habitId, completedAt, minutes, memo)` /
  `record.update(completedAt, minutes, memo)` 시그니처로 흡수. 도메인 어휘는
  `HabitRecord` 한쪽으로 통일했다. (DB 테이블/API URL 호환을 위해 “records”
  명칭은 유지, 어그리게이트 이름과 정합성을 맞추려면 추후 일괄 rename 필요.)

---

## 3. 헥사고날 관점

### 3.1 잘 된 점

- **포트가 도메인 객체를 그대로 노출**한다. `HabitRepository`, `AuthAccountRepository`
  등이 entity가 아닌 도메인을 반환 → 응용 계층이 인프라에 종속되지 않음.
- **JPA 어댑터가 매퍼로 도메인↔엔티티 변환**을 책임진다 (`HabitMapper`,
  `HabitRecordMapper`, `AuthAccountMapper`, `UserMapper`).
- **외부 시스템 어댑터를 `port.out`으로 추상화** — Kakao OAuth(`KakaoOAuthPort`),
  내부 user-service 호출(`SocialUserCreatePort`), JWT(`TokenGeneratePort`,
  `TokenParsePort`), Redis(`AuthSessionRepository`) 등.
- **컨트롤러는 얇다.** 요청 DTO → Command → use case → 응답 DTO 변환만 한다.

### 3.2 개선 필요

#### (H-1) `KakaoAuthorizeUseCase`가 use case 패턴을 깨뜨림 — ✅ 수정 완료
```java
public interface KakaoAuthorizeUseCase {
    String buildAuthorizeUrl(String state);
}
```
- 다른 use case는 모두 `Command`/`Result` DTO와 `execute(...)` 시그니처. 여기만
  raw `String`.
- 서비스 구현은 `KakaoOAuthPort`로 그대로 위임 — use case 추상화의 가치가 없다.
- **권장**: `KakaoAuthorizeCommand(String state)` /
  `KakaoAuthorizeResult(String url)` 도입하거나, 컨트롤러에서 포트를 직접 주입
  받지 말고 use case 한 단계만 두되 서명을 다른 use case와 통일.
- **수정 내용**: `KakaoAuthorizeCommand(String state)` /
  `KakaoAuthorizeResult(String authorizeUrl)` record 도입.
  `KakaoAuthorizeUseCase.execute(Command)` 시그니처로 통일하고
  `KakaoAuthorizeService` · `AuthController`도 새 계약에 맞춰 갱신.

#### (H-2) `LogoutService`가 추상화를 우회한다 — ✅ 수정 완료
- `ReissueTokenService`는 `TokenParsePort`로 토큰을 해석하지만,
  `LogoutService`는 `JwtProvider`(security 모듈의 구체)를 직접 주입한다.
- 같은 service 패키지에서 두 가지 추상화 레벨이 공존 → 일관성 깨짐.
- **권장**: `LogoutService`도 `TokenParsePort`로 `tokenId` 추출. 만약
  refresh-only 검증이 필요하면 `TokenParsePort`에 메서드 추가 (이미
  `parseRefreshToken`이 동일 검증을 함 → 재사용 가능).
- **수정 내용**: `LogoutService`가 `JwtProvider` 의존 제거 후
  `TokenParsePort.parseRefreshToken`으로 일원화. `ReissueTokenService`와
  동일한 추상화 레벨로 정렬되어 application 계층에서 security 모듈 구체에
  더는 의존하지 않는다.

#### (H-3) `AuthController#logout`이 도메인 예외를 직접 처리 — ✅ 수정 완료
```java
} catch (BaseException exception) {
    if (!"INVALID_REFRESH_TOKEN".equals(exception.getErrorCode().name())) {
        throw exception;
    }
}
```
- “이미 만료된 토큰으로 로그아웃해도 200” 정책을 컨트롤러가 처리. 응용 계층이
  표현해야 할 정책이다.
- **권장**: `LogoutService`가 invalid-token을 무시하도록 만들거나(idempotent
  logout), use case 결과 타입에 “이미 로그아웃됨” 케이스를 표현.
- **수정 내용**: `LogoutService`가 `INVALID_REFRESH_TOKEN`을 내부에서 잡고
  세션이 없으면 `ifPresent`로 조용히 종료하도록 멱등화. `AuthController#logout`
  의 `try/catch`/swallow 로직 제거.

#### (H-4) `UserRepository.save(User)`가 `Long`을 반환 — ✅ 수정 완료
- 포트가 JPA 자동생성 PK(`Long`)를 그대로 노출 — 어댑터 세부 사항이 도메인
  포트로 새어 나옴.
- 이 `Long`은 auth-service가 외래 키로 보관하기 위한 값이라 “MSA 식별자
  계약”으로 의미가 있긴 하다. 그래도 도메인 어휘는 `UserId(publicId)`다.
- **권장**: `User.create(...)`가 `internalId`까지 포함하도록 정리하거나, port가
  `User`를 반환하고 caller가 `user.getId()` / `user.getInternalId()`를 꺼내게
  한다.
- **수정 내용**: `User`에 nullable `internalId` 필드와 `User.from(internalId, ...)`
  팩토리 추가. `UserRepository.save(User)` 시그니처가 `User`를 반환하고
  caller(`CreateUserService`)는 `saved.getInternalId()`로 꺼내 사용. JPA
  어댑터/매퍼는 영속화된 엔티티를 도메인으로 다시 매핑.

#### (H-5) `GetCurrentUserUseCase`가 raw `String` 인자 — ✅ 수정 완료
- 다른 use case는 모두 Command 객체를 받는데 여기만 `getCurrentUser(String publicId)`.
- **권장**: `GetCurrentUserCommand(String publicId)` 도입 + `execute(...)` 시그니처
  통일.
- **수정 내용**: `GetCurrentUserCommand(String publicId)` record 도입(컴팩트
  생성자에서 null 검증). `GetCurrentUserUseCase.execute(Command)` 시그니처로
  통일하고 `UserController`/`GetCurrentUserService`도 일치시켰다.

#### (H-6) Use case 메서드 시그니처 통일 — ✅ 수정 완료
- habit-service: `execute(Command)` 일관됨. ✅
- auth/user-service: `login` / `issue` / `reissue` / `logout` / `changeRole`
  / `getCurrentUser` / `buildAuthorizeUrl` 등 메서드명이 제각각.
- **권장**: 모든 use case를 `execute(Command)`로 통일하면 컨트롤러/테스트 패턴이
  단순해진다 (선호 사항이므로 강제는 아님).
- **수정 내용**: `KakaoLoginUseCase` · `ReissueTokenUseCase` ·
  `AuthTokenIssueUseCase` · `LogoutUseCase` · `ChangeUserRoleUseCase` ·
  `KakaoAuthorizeUseCase` · `GetCurrentUserUseCase`까지 모두 `execute(Command)`
  로 일원화. 컨트롤러/구현체/테스트가 동일한 호출 패턴을 따른다.

#### (H-7) 포트 메서드 인자가 raw String — ✅ 수정 완료
- `AuthAccountRepository.findByProviderAndProviderId(String provider, ...)` —
  `AuthProvider` enum 인자로 받아야 호출부에서 `AuthProvider.KAKAO.name()`
  변환을 강요하지 않는다 (`KakaoLoginService`에 그 변환이 노출돼 있다).
- `HabitRecordRepositoryCustomImpl.findCompletionsForOwnerBetweenDates(String ownerType, ...)`
  도 마찬가지. `OwnerType` enum을 그대로 받자.
- **수정 내용**: `AuthAccountRepository.findByProviderAndProviderId`가
  `AuthProvider` enum을 받도록 수정 (`KakaoLoginService`의 `.name()` 변환
  제거, JPA 어댑터 내부에서만 `provider.name()` 호출). 마찬가지로
  `HabitRecordRepositoryCustom.findCompletionsForOwnerBetweenDates`가
  `OwnerType` enum을 받고 어댑터/캐스팅이 사라졌다.

#### (H-8) `findById` + `loadById` 중복 — ✅ 수정 완료
- `HabitRepository`, `HabitRecordRepository` 모두 `findById(Optional)` +
  `loadById(throw)` 두 종류 메서드를 노출한다.
- 호출부에서는 거의 항상 `loadById`만 사용 (`findById`는 거의 사용되지 않음).
- **권장**: `loadById`만 남기거나, 명확한 정책으로 분리. (현 정책이 의도라면
  Javadoc으로 명시.)
- **수정 내용**: 두 포트의 `findById`(Optional) 시그니처를 제거하고
  `loadById`만 노출. 어댑터(`HabitJpaAdapter`/`HabitRecordJpaAdapter`)는
  내부에서 `findById` 호출 후 도메인 예외로 직접 변환.

#### (H-9) 매퍼/DTO 중복 — ✅ 수정 완료
- `CreateHabitResult.from`, `UpdateHabitNameService#toResult`, `GetHabitsService#toDto`,
  `GetHabitDetailService` 등 모두 동일한 `Habit` → DTO 변환을 반복한다.
- **권장**: `HabitView`(공통 DTO) 한 개를 application 계층에서 정의해 모든
  Result가 이를 사용하거나, MapStruct/공통 DtoFactory로 위임.
- **수정 내용**: 공통 `HabitView` record를 `application/port/in/dto`에 도입하고
  `HabitView.from(Habit)` / `HabitView.from(List<Habit>)` 팩토리로 변환을
  일원화. `CreateHabitResult` · `UpdateHabitNameResult` ·
  `GetHabitDetailResult` · `GetHabitsResult` · `SwapHabitIndexResult`가
  `HabitView`를 그대로 보관. 응답 DTO/서비스에서 중복 매핑 제거.

---

## 4. MSA 관점

### 4.1 잘 된 점

- 서비스마다 독립 DB, 독립 도메인, Eureka 기반 디스커버리, Gateway 단일
  진입점 — 정석적인 구성.
- **`InternalUserController`**(`/internal/users`)와
  **`UserController`**(`/api/users/me`)를 패키지/URL prefix로 분리한 점은 좋다.

### 4.2 개선 필요

#### (M-1) auth-service가 user-service의 PK를 직접 저장
```java
@Entity @Table(name = "auth_users")
public class AuthAccountEntity {
    @Id @Column(updatable=false) private Long userId; // ← user-service의 PK
    ...
}
```
- user-service의 내부 식별자(`Long`)가 auth-service DB까지 흘러간다. user-service가
  PK 전략을 바꾸면 auth-service 스키마가 깨진다 — 강한 결합.
- **권장**: 서비스 간 식별자는 `publicId`(UUID) 한 가지만 공유. auth-service는
  자체 PK를 별도로 가지고, FK는 `publicId(string)` 한 컬럼만 보관한다.

#### (M-2) 내부 통신 URL이 환경변수 fallback `localhost:8081`
```java
@Value("${app.user-service.base-url:http://localhost:8081}") String userServiceBaseUrl
```
- Gateway/Eureka 환경에서 다른 서비스는 `lb://service-name`을 쓰지만, 여기는
  hardcoded localhost로 fallback이 가능 → 운영 환경에서 사고 발생 가능.
- **권장**: `WebClient`/`RestClient`에 LoadBalanced 빈을 사용하고
  `lb://user-service`로 호출. fallback URL을 두지 않는다.

#### (M-3) `InternalUserController`에 보안 미적용
- 인증/인가 어노테이션이 없다. Gateway가 잘못 라우팅하면 외부에서도 호출 가능.
- **권장**:
  1. `/internal/**` 경로를 Gateway에서 명시적으로 차단.
  2. 서비스 간 호출용 토큰/mTLS 도입.
  3. SecurityConfiguration에서 `/internal/**`는 내부망 IP 화이트리스트로 한정.

#### (M-4) Kakao 로그인 1회당 토큰/유저 정보 호출 2번 + 동시 로그인 충돌 처리
- `KakaoLoginService`는 신규 사용자 시 user-service에 HTTP 호출하여 사용자 생성,
  실패 시 `AuthAccountJpaAdapter`에서 동시성 충돌을 다시 update로 회복한다.
- 분산 트랜잭션이 아니라 **eventually-consistent**한 보상 패턴이지만, 명세가
  없다 — 사용자 생성에 성공하고 auth_users 저장에 실패하면 user-service에
  orphan user가 남는다.
- **권장**: outbox 패턴 또는 Kafka 이벤트 기반(이미 모듈은 존재) 비동기 처리,
  혹은 idempotency-key를 `providerId`로 부여하고 user-service `POST /internal/users`를
  멱등하게 만든다.

#### (M-5) Kafka 모듈은 있으나 사용처 0건
- `modules/kafka` 의존성 / `docker-compose`의 Kafka는 있지만, 실제 produce/consume
  코드는 모든 서비스에서 발견되지 않음.
- **권장**: 사용 계획을 README/CLAUDE에 명시하거나, 단기간 사용 계획이 없으면
  의존성을 제거.

---

## 5. 데드 코드 / 일관성 이슈

| 항목 | 위치 | 조치 |
|---|---|---|
| `RefreshToken` (도메인) | `auth-service/domain/RefreshToken.java` | **삭제** — Redis 기반 `AuthSession`으로 대체됨 |
| `RefreshTokenRepository` | `application/port/out` | **삭제** |
| `RefreshTokenJpaAdapter` | `infrastructure/adapter/out/persistence` | **삭제** (`@Repository` 어노테이션도 누락 → 미주입 확인) |
| `RefreshTokenJpaRepository` | 동일 디렉토리 | **삭제** |
| `RefreshTokenEntity` | 동일 디렉토리 + `refresh_tokens` 테이블 | **삭제** + 마이그레이션 |
| `HabitRecordRepository#findByHabitIdAndCompletedAt` | port + adapter + JpaRepository | **삭제** (호출처 없음) |
| `HabitRecordEntity#completed` | 항상 true (`HabitRecordMapper.COMPLETED`) | 컬럼 삭제 또는 의미 부여 |
| `HabitController#userId(JwtPrincipal)` | habit-service controller | 메서드명을 `ownerPublicId`로 변경 (실제 반환은 publicId) |
| `Asia/Seoul` 하드코딩 | `HabitRecordEntity` + `CreateHabitRecordService` | 공통 상수 또는 `time` 모듈로 이동 |
| `AuthErrorCode.KAKAO_LOGIN_NOT_CONFIGURED` | 정의만 있고 사용처 없음 | 사용 또는 삭제 |
| `AuthAccountJpaAdapter#save`의 `@Transactional` | 어댑터에 트랜잭션 어노테이션 | 트랜잭션 경계는 application service에 있는 편이 헥사고날 원칙. 어댑터에서는 제거 가능 |

---

## 6. 우선순위별 리팩토링 권고

### 🔴 High — 정합성/안정성에 직결
1. **(M-1)** auth-service에서 user-service PK(`Long`) 의존 제거 → `publicId` 중심
   설계로 전환.
2. **데드 코드 제거** — `RefreshToken*` 5개 파일 + 테이블 마이그레이션.
3. **(M-3)** `/internal/**` 경로 보안 (Gateway 라우팅 + Security 설정).
4. **(M-2)** user-service URL `lb://`로 통일.

### 🟡 Mid — 헥사고날·DDD 일관성
6. **(D-1)** `User`, `AuthAccount`에 VO 도입 + 도메인 메서드 추가.
7. **(D-2)** `AuthSession*Exception`을 인프라로 이동.
8. **(H-1)(H-5)** `KakaoAuthorizeUseCase` / `GetCurrentUserUseCase`도 Command·Result
   패턴으로 통일.
9. **(H-2)(H-3)** Logout 흐름을 use case 안으로 끌어들이고 `TokenParsePort`로
   추상화.
10. **(H-7)** Repository 포트의 raw String 인자를 enum으로 교체.

### 🟢 Low — 가독성/유지보수
11. **(D-4)** `HabitMemo`의 null 패턴을 Optional 또는 empty-VO로 정리.
12. **(D-5)** Record vs Completion 어휘 통일.
13. **(H-9)** `Habit → DTO` 변환 코드 중복 제거.
14. **(H-8)** `findById`/`loadById` 정책 명문화.
15. `Asia/Seoul` 상수 공통화, `HabitRecordEntity#completed` 정리, 로그아웃
   컨트롤러 swallow 로직 제거.
16. (M-5) Kafka 모듈 사용 계획 문서화 또는 의존성 제거.

---

## 7. 참고 — 잘 잡힌 디테일

- `Habit.assertOwner` / `HabitOwner.user(...)` / `HabitId.newId()`처럼 도메인이
  스스로 invariant를 표현하는 메서드명.
- `AuthAccountJpaAdapter`가 `DataIntegrityViolationException` 동시 INSERT 충돌을
  retry로 회복하는 시도 (보상 흐름 인지).
- `JwtTokenAdapter`가 `TokenGeneratePort`/`TokenParsePort`를 동시에 구현하면서
  application 코드에는 두 추상화로 분리 노출 — 인터페이스 분리 원칙(ISP) 준수.
- `BaseException` + `ProblemDetail` + `MDC traceId` 결합 — observability 친화적.
- 모듈 구성(`error-handling`, `security`, `jpa`, `kafka`, `time`, `swagger`,
  `redis`)이 잘게 쪼개져 서비스가 필요한 것만 의존.
