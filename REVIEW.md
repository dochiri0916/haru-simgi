# Code Review: haru-simgi

> 리뷰 일자: 2026-04-10
> 대상: 전체 코드베이스 (6개 서비스, 5개 공통 모듈)

---

## 1. 종합 평가

| 영역 | 점수 | 요약 |
|------|------|------|
| **아키텍처** | A | 헥사고날 아키텍처 일관 적용, 모듈 분리 우수 |
| **도메인 설계** | A- | 불변 객체, Value Object, 팩토리 메서드 잘 활용 |
| **보안** | C+ | Gateway 토큰 미검증, 하드코딩된 시크릿, 내부 API 무인증 |
| **테스트** | C | auth/user 일부만 존재, habit-service 테스트 전무 |
| **에러 처리** | B | RFC 7807 준수, 일부 서비스 간 통신 에러 처리 부족 |
| **운영 준비도** | C+ | 분산 추적/서킷 브레이커/Rate Limiting 미적용 |

---

## 2. 잘된 점

### 2.1 일관된 헥사고날 아키텍처

모든 서비스가 동일한 계층 구조를 따른다:

```
domain/              # 순수 비즈니스 로직, 프레임워크 무관
application/
  ├─ port/in/        # 유스케이스 인터페이스
  ├─ port/out/       # 아웃바운드 포트 (레포지토리 추상화)
  └─ service/        # 유스케이스 구현
infrastructure/
  └─ adapter/
     ├─ in/web/      # REST 컨트롤러
     └─ out/persistence/  # JPA 어댑터
```

- 도메인이 인프라에 의존하지 않아 테스트와 교체가 용이하다.
- 포트/어댑터 패턴으로 외부 시스템(Kakao OAuth, User Service 등)과의 결합도가 낮다.

### 2.2 불변 도메인 모델

- Java record와 `@RequiredArgsConstructor(access = AccessLevel.PRIVATE)`로 불변성 보장
- `Habit.create()` / `Habit.from()` 같은 팩토리 메서드로 생성 의도 명확화
- Value Object(`HabitId`, `HabitName`, `HabitOwner`)에 생성 시점 유효성 검증 내장

```java
// HabitName.java - 좋은 예시
public record HabitName(String value) {
    public HabitName {
        if (value == null || value.isBlank()) throw ...;
        if (value.length() > 50) throw ...;
    }
}
```

### 2.3 공통 모듈 설계

- **Spring Boot Auto-configuration 패턴** 일관 적용
- `@ConditionalOnMissingBean`, `@ConditionalOnProperty`로 충돌 방지
- `@ConfigurationProperties` record로 타입 안전한 설정 관리
- 각 서비스가 필요한 모듈만 선택적으로 의존

### 2.4 보안 모듈 (modules/security)

- JWT 생성/검증/쿠키/Bearer 이중 지원
- `JwtProvider` 테스트 17개 — role 정규화, 만료, 토큰 타입 등 엣지케이스 커버
- `RefreshTokenVerifier`로 리프레시 토큰 검증 분리
- `JwtAuthenticationFilter`에서 쿠키 → Bearer 폴백 메커니즘

### 2.5 Kafka 모듈

- 토픽/에러핸들링/리스너/프로퍼티 분리된 Auto-configuration
- DLQ(Dead Letter Queue) 조건부 활성화
- `ApplicationContextRunner` 기반 통합 테스트 4개

### 2.6 기타

- `@Transactional(readOnly = true)` 조회 서비스에 적용 (user-service)
- Eureka 로드밸런싱 + Config Server 외부 설정 관리
- Jenkinsfile 파라미터화 빌드 파이프라인
- Virtual Thread 활성화 (Java 21)
- Docker Compose 헬스체크 및 기동 순서 관리

---

## 3. 심각한 문제 (Critical)

### 3.1 Gateway: 토큰 존재만 확인, 유효성 미검증

**파일**: `gateway/.../AuthRequiredGatewayFilterFactory.java:29-45`

```java
String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
    return chain.filter(exchange);  // 토큰 내용 검증 없이 통과
}
```

Gateway가 토큰의 **존재 여부**만 확인하고 **서명/만료/유효성을 검증하지 않는다**. 만료되거나 위조된 토큰으로도 Gateway를 통과할 수 있다.

**권장**: Gateway에서 JWT 파싱 및 검증 수행. 최소한 서명과 만료 시간은 확인해야 한다.

### 3.2 auth-service: 테스트 컴파일 실패

**파일**: `auth-service/.../KakaoLoginServiceTest.java:44`

- `AuthTokenIssuer` 클래스를 참조하나 실제 클래스명은 `AuthTokenIssuerService`
- `ReissueTokenServiceTest`도 리팩토링 후 생성자 시그니처 불일치
- 테스트 빌드 자체가 실패하는 상태

### 3.3 보안: 하드코딩된 시크릿/자격증명

**파일**: `docker-compose.yml`, 각 서비스 `application.yml`

| 항목 | 위치 | 값 |
|---|---|---|
| JWT_SECRET | docker-compose.yml | `12345678901234567890123456789012` |
| Kakao REST API Key | auth-service/application.yml | 실제 키 노출 |
| Kakao Client Secret | auth-service/application.yml | 실제 시크릿 노출 |
| Config/Eureka 비밀번호 | docker-compose.yml | `adbin` |

학습 프로젝트라 하더라도, Git에 커밋된 시크릿은 즉시 로테이션해야 한다.

---

## 4. 주요 문제 (High)

### 4.1 user-service: 내부 API 무인증 노출

**파일**: `user-service/.../application.yml:13-15`

`/internal/users/social`이 `public-endpoints`에 등록되어 있어, 인증 없이 사용자 생성이 가능하다. 내부 서비스 간 통신이라도 API 키 또는 내부 토큰 검증이 필요하다.

### 4.2 auth-service: 동시 로그인 Race Condition

**파일**: `auth-service/.../AuthAccountJpaAdapter.java:21-69`

`save()` 메서드가 `userId`(PK) 기준으로 조회 후 저장하지만, unique 제약은 `(provider, providerUserId)`에 걸려 있다. 동일 카카오 계정으로 동시 로그인 시 두 개의 다른 userId로 시도할 수 있어, retry 로직이 잘못된 결과를 반환할 수 있다.

### 4.3 입력 검증 부재 (전체 서비스)

대부분의 Request DTO에 Bean Validation 어노테이션(`@NotBlank`, `@Size` 등)이 없다:

- `CreateSocialUserRequest` (user-service) — `@Valid` 사용하나 검증 어노테이션 없음
- `CreateHabitRequest`, `CreateHabitRecordRequest` (habit-service) — 검증 전무
- `RefreshTokenRequest` (auth-service) — `requireNonNull`만 있고 빈 문자열 미검증

### 4.4 user-service: 빈 문자열 → null 변환 버그

**파일**: `user-service/.../CreateSocialUserRequest.java:11-14`

```java
public CreateSocialUserCommand toCommand() {
    return new CreateSocialUserCommand(
        StringUtils.hasText(nickname) ? nickname : null,      // 빈 문자열 → null
        StringUtils.hasText(profileImageUrl) ? profileImageUrl : null
    );
}
```

`CreateSocialUserCommand`의 compact constructor가 `requireNonNull`로 null을 거부하므로, 빈 문자열 입력 시 NPE가 발생한다.

### 4.5 habit-service: 시스템 타임존 의존

**파일**: `habit-service/.../GetHabitGrassService.java:26-27`

```java
Instant fromInstant = command.fromDate().atStartOfDay(ZoneId.systemDefault()).toInstant();
```

`ZoneId.systemDefault()`는 서버 환경에 따라 달라진다. 같은 데이터가 서버마다 다른 잔디를 보여줄 수 있다. 사용자 타임존을 파라미터로 받거나, `modules/time`의 Clock 빈을 사용해야 한다.

### 4.6 테스트 커버리지 부족

| 서비스 | 테스트 상태 |
|---|---|
| auth-service | 4개 테스트 파일 있으나 컴파일 실패 |
| user-service | 1개 happy-path 테스트만 존재 |
| habit-service | **테스트 파일 전무** |
| modules/security | 우수 (17개 테스트) |
| modules/kafka | 양호 (4개 통합 테스트) |
| gateway | 테스트 없음 |

---

## 5. 중간 수준 문제 (Medium)

### 5.1 Gateway: 에러 응답 형식 불일치

**파일**: `gateway/.../AuthRequiredGatewayFilterFactory.java:50-52`

하드코딩된 JSON 응답이 `error-handling` 모듈의 RFC 7807 ProblemDetail 형식과 다르다. 클라이언트가 일관된 에러 형식을 기대할 수 없다.

### 5.2 보안 모듈: Role 정규화 취약점

**파일**: `modules/security/.../JwtProvider.java:147-165`

`ROLE_` 접두사를 `startsWith`로 제거하는데, `ROLE_ROLE_ADMIN` 같은 입력에서 한 번만 제거되어 `ROLE_ADMIN`이 남는다. 엄격한 정규식 검증이 필요하다.

### 5.3 auth-service: 보안 이벤트 감사 로그 부재

로그인 시도(성공/실패), 토큰 재발급, 로그아웃, 역할 변경 등 보안 이벤트에 대한 감사 로그가 없다. `ChangeUserRoleService`에서 누가 어떤 사용자의 역할을 변경했는지 추적할 수 없다.

### 5.4 auth-service: 외부 서비스 실패 처리 부재

**파일**: `auth-service/.../KakaoLoginService.java:55-68`

`socialUserCreatePort.create()` 호출 시 User Service 장애에 대한 처리가 없다. null 반환 시 NPE, 타임아웃 시 무한 대기 가능성이 있다.

### 5.5 서비스 간 통신 안전장치 부재

- **서킷 브레이커** 미적용 — 하나의 서비스 장애가 전파될 수 있음
- **분산 추적** 미적용 — 요청 흐름 추적 불가
- **Rate Limiting** 미적용 — Gateway에 요청 제한 없음

### 5.6 habit-service: `@Transactional` 누락

`DeleteHabitService`에서 습관 기록 삭제 후 습관 삭제를 수행하는데, `@Transactional`이 없어 중간 실패 시 데이터 정합성이 깨질 수 있다.

### 5.7 habit-service: 미사용 DTO

`LogHabitCommand`, `LogHabitResult`가 존재하나 어떤 서비스에서도 사용하지 않는다 (Dead Code).

### 5.8 habit-service: `HabitRecordEntity`에 FK 없음

`habit_completions` 테이블에 `habits` 테이블에 대한 외래 키 제약이 없다. `DeleteHabitService`가 수동으로 처리하지만, DB 레벨 참조 무결성이 보장되지 않는다.

### 5.9 Gateway: CORS 이중 설정

Gateway의 `GatewayCorsConfiguration`(reactive)과 security 모듈의 `CorsAutoConfiguration`(servlet)이 공존한다. Reactive 컨텍스트에서 어느 것이 우선하는지 불명확하다.

---

## 6. 경미한 문제 (Low)

| 문제 | 위치 | 설명 |
|------|------|------|
| 로깅 부재 | 전체 서비스 레이어 | 서비스 계층에 info/debug 로그 없음. 프로덕션 디버깅 어려움 |
| 매직 넘버 | `UserEntity.java` | 컬럼 길이(36, 100, 500) 상수 미정의 |
| 에러코드 문자열 비교 | `AuthController.java:103` | `"INVALID_REFRESH_TOKEN".equals(...)` — enum 비교가 안전 |
| `@Component` vs `@Repository` | `HabitRecordJpaAdapter` | `@Component` 사용, `HabitJpaAdapter`는 `@Repository` — 불일치 |
| SpringDoc 미활용 | user-service | `springdoc-openapi` 의존성 있으나 `@OpenAPI` 어노테이션 없음 |
| 컨트롤러 생성자 | `HabitController` | 8개 의존성 수동 주입 — `@RequiredArgsConstructor` 사용 권장 |
| Config Server Git 타임아웃 | `config-server/application.yml` | `clone-on-start: true`에 타임아웃 미설정 — Git 접근 불가 시 기동 중단 |
| Time 모듈 타임존 검증 | `TimeAutoConfiguration.java` | 잘못된 타임존 문자열 시 `ZoneId.of()` 예외 발생, 가드 없음 |

---

## 7. 서비스별 요약

### auth-service

| 항목 | 상태 |
|------|------|
| 아키텍처 | 헥사고날, 포트/어댑터 잘 적용 |
| 핵심 기능 | 카카오 OAuth, JWT 발급/재발급/로그아웃, 역할 변경 |
| 주요 장점 | 쿠키/Bearer 이중 전송, 프로필 기반 Dev 컨트롤러 |
| 주요 문제 | 테스트 컴파일 실패, 하드코딩 시크릿, Race Condition |

### user-service

| 항목 | 상태 |
|------|------|
| 아키텍처 | 헥사고날, 깔끔한 계층 분리 |
| 핵심 기능 | 소셜 사용자 생성, 현재 사용자 조회 |
| 주요 장점 | 불변 도메인, readOnly 트랜잭션, 팩토리 메서드 |
| 주요 문제 | 내부 API 무인증, 입력 검증 미비, 테스트 부족 |

### habit-service

| 항목 | 상태 |
|------|------|
| 아키텍처 | 헥사고날, 8개 유스케이스 구현 |
| 핵심 기능 | 습관 CRUD, 기록, 잔디 시각화 |
| 주요 장점 | Value Object 검증, QueryDSL, 소유권 검증 (일부) |
| 주요 문제 | 잔디 집계 버그, 인증 누락 엔드포인트, 테스트 전무 |

### gateway

| 항목 | 상태 |
|------|------|
| 아키텍처 | Spring Cloud Gateway + Eureka 라우팅 |
| 주요 장점 | 타임아웃 설정, CORS 중복 헤더 방지, 서비스 디스커버리 |
| 주요 문제 | **토큰 유효성 미검증 (Critical)**, 에러 응답 형식 불일치 |

### 공통 모듈

| 모듈 | 평가 |
|------|------|
| error-handling | RFC 7807 준수, `BaseException` 잘 설계됨 |
| security | JWT 구현 우수, 테스트 충실. Role 정규화 취약점 존재 |
| jpa | BaseEntity/QueryDSL Auto-config 양호 |
| kafka | DLQ/재시도/토픽 설정 잘 분리됨, 테스트 존재 |
| time | 단순하고 깔끔. 타임존 검증 추가 필요 |

### 인프라 서비스

| 서비스 | 평가 |
|------|------|
| config-server | 기본 인증 적용, Git 기반 설정 관리. 비밀번호 복잡도 검증 부족 |
| eureka-server | 서비스 디스커버리 정상. 악의적 서비스 등록 방어 없음 |

---

## 8. 우선순위별 개선 로드맵

### 즉시 수정 (P0)

1. **Gateway 토큰 검증 추가** — JWT 서명/만료 최소 검증
2. **auth-service 테스트 컴파일 오류 수정** — 리팩토링 후 테스트 동기화

### 단기 (1~2주)

5. **하드코딩 시크릿 제거** — 환경변수 전용, 기본값 제거, Kakao 키 로테이션
6. **Request DTO 입력 검증 추가** — `@NotBlank`, `@Size` 등 Bean Validation 적용
7. **내부 API 인증 추가** — user-service `/internal/**`에 서비스 간 인증 적용
8. **habit-service 단위 테스트 작성** — 도메인/서비스/컨트롤러 최소 커버리지 확보
9. **`@Transactional` 누락 서비스에 추가** — DeleteHabitService 등

### 중기 (2~4주)

10. **auth-service Race Condition 수정** — `persist()`와 `retryUpdateAfterConcurrentInsert()` 모두 `userId`(PK) 대신 `(provider, providerUserId)` 기준으로 조회하도록 변경. 동시 요청 시 userId가 다르게 생성되더라도 동일 레코드를 찾아 upsert할 수 있어야 한다.
11. **분산 추적 도입** — Micrometer Tracing (구 Sleuth) 적용
12. **Gateway Rate Limiting 추가** — Spring Cloud Gateway RequestRateLimiter
13. **보안 감사 로그 추가** — 로그인/로그아웃/역할 변경 이벤트 기록
14. **서킷 브레이커 도입** — Resilience4j로 서비스 간 호출 보호

### 장기

15. 에러 응답 형식 전체 통일 (Gateway 포함)
16. API 버저닝 전략 수립
17. E2E 통합 테스트 파이프라인 구축
18. 서비스 메시 검토 (mTLS, 서비스 간 인증 자동화)

---

## 9. 결론

**아키텍처 설계는 학습 프로젝트 수준을 넘어 실무 수준에 가깝다.** 헥사고날 아키텍처의 일관된 적용, 불변 도메인 모델, 모듈화된 공통 라이브러리는 잘 설계되어 있다.

개선이 필요한 핵심 영역은 **보안 (Gateway 토큰 검증, 시크릿 관리, 내부 API 인증)**과 **테스트 커버리지**다. 특히 Gateway의 토큰 미검증은 인증 체계의 가장 기본적인 부분이므로 우선 수정이 필요하다.

잔디 집계 로직의 값 덮어쓰기 버그는 서비스의 핵심 기능에 영향을 주므로 함께 빠르게 수정해야 한다.

이 문제들이 해결되면, 전체적으로 잘 구조화된 MSA 프로젝트로 발전할 수 있는 좋은 기반을 갖추고 있다.
