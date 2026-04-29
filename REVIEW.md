# 프로젝트 리팩토링 리뷰

## 요약

전체 구조는 Gradle 멀티 모듈과 Spring Cloud 기반 MSA로 잘 분리되어 있고, `application/port`, `application/service`, `infrastructure/adapter`, `domain` 패키지 경계도 비교적 일관됩니다. 리팩토링의 핵심은 새 아키텍처를 도입하기보다 이미 만든 공통 모듈을 더 적극적으로 활용해서 중복과 운영 위험을 줄이는 쪽이 적절합니다.

우선순위는 다음 순서가 좋습니다.

1. 인증/내부 API/세션 저장소의 운영 안정성 보강
2. 서비스별 SecurityFilterChain 중복 제거
3. 내부 HTTP 클라이언트 오류 처리와 타임아웃 표준화
4. habit-service 정렬/잔디 집계 성능 개선
5. 설정 기본값, 테스트, 문서의 실제 동작 일치화

## P0: 먼저 손봐야 할 안정성 이슈

### 1. 내부 API 토큰 기본값 제거

- 근거: `auth-service/src/main/resources/application.yml:36`, `habit-service/src/main/resources/application.yml`, `user-service/src/main/resources/application.yml`에서 `INTERNAL_API_TOKEN` 기본값이 `internal-default-token-change-me`로 잡혀 있습니다.
- 위험: 환경 변수 누락 시 내부 API 인증이 예측 가능한 토큰으로 동작합니다. 운영 설정 실수 하나가 서비스 간 내부 API 전체 노출로 이어질 수 있습니다.
- 제안:
  - 운영/공용 compose에서는 기본값 없이 `${INTERNAL_API_TOKEN:?INTERNAL_API_TOKEN is required}` 형태로 강제합니다.
  - 로컬 전용 profile에만 명시적인 dev 토큰을 둡니다.
  - `InternalApiClientProperties`, `InternalApiServerProperties`에 `@NotBlank` 검증을 추가합니다.

### 2. Gateway JWT 검증 로직 중복 제거

- 근거: `gateway/src/main/java/com/dochiri/gateway/filter/AuthRequiredGatewayFilterFactory.java:47`부터 직접 `jwt.secret`을 받아 `Jwts.parser()`로 검증합니다. 반면 servlet 서비스들은 `modules/security`의 `JwtProvider`, `JwtAuthenticationFilter`를 사용합니다.
- 위험: JWT claim 이름, category 검증, 예외 메시지, secret 검증 규칙이 gateway와 서비스에서 갈라질 수 있습니다.
- 제안:
  - `modules/security`에 순수 JWT 검증 컴포넌트를 분리하고 gateway도 같은 컴포넌트를 사용합니다.
  - WebFlux용 인증 실패 응답 writer를 공통화해 `ProblemDetail` 응답 포맷도 servlet 서비스와 맞춥니다.
  - `@Value` 대신 `@ConfigurationProperties`를 사용해 gateway 설정 검증을 추가합니다.

### 3. Redis 세션 갱신을 원자적으로 처리

- 근거: `AuthSessionRedisAdapter.saveReplacingUserSessions()`는 `deleteByPublicId()` 후 session, refresh, userSessions 키를 순차 저장합니다. 관련 코드: `auth-service/src/main/java/com/dochiri/authservice/infrastructure/adapter/out/cache/AuthSessionRedisAdapter.java:33`.
- 위험: 중간에 Redis 장애가 나면 기존 세션 삭제 후 새 세션 일부만 저장될 수 있습니다. 동시에 로그인/재발급이 들어오면 user session set과 실제 session key가 어긋날 수도 있습니다.
- 제안:
  - Redis transaction, Lua script, 또는 `SessionStore` 수준의 단일 원자 연산으로 묶습니다.
  - `deleteByPublicId`와 신규 저장을 분리하지 말고 “사용자 단일 활성 세션 교체”라는 명령으로 명확히 모델링합니다.
  - 동시 로그인/재발급 테스트를 추가합니다.

### 4. 외부 호출이 포함된 트랜잭션 경계 축소

- 근거: `SocialAccountProvisioner.authenticateKakao()`가 `@Transactional` 안에서 카카오 인증, user-service 내부 호출, auth account 저장을 모두 수행합니다. 관련 코드: `auth-service/src/main/java/com/dochiri/authservice/application/service/SocialAccountProvisioner.java:25`.
- 위험: DB 트랜잭션이 외부 HTTP 지연 시간만큼 길어지고, user-service 생성 성공 후 auth-service 저장 실패 같은 분산 일관성 문제가 남습니다.
- 제안:
  - 카카오 인증은 트랜잭션 밖에서 수행합니다.
  - `find-or-create auth account`만 짧은 트랜잭션으로 묶습니다.
  - user-service 생성과 auth account 저장 사이 실패에 대해 재시도 가능한 보상 흐름 또는 idempotency 기반 재진입 흐름을 명시합니다.

## P1: 중복 제거와 설계 정리

### 5. 서비스별 SecurityFilterChain 템플릿화

- 근거: `AuthServiceSecurityConfiguration`, `UserServiceSecurityConfiguration`, `HabitServiceSecurityConfiguration`가 거의 같은 설정을 반복합니다. 대표적으로 `auth-service/.../AuthServiceSecurityConfiguration.java:30`, `user-service/.../UserServiceSecurityConfiguration.java:30`, `habit-service/.../HabitServiceSecurityConfiguration.java:31` 이후가 동일 패턴입니다.
- 문제: 내부 API 필터 순서, public endpoint, 예외 처리 설정 변경 시 서비스마다 수정해야 합니다.
- 제안:
  - `modules/security`에 `SecurityFilterChainBuilder` 또는 `SecurityChainCustomizer`를 둡니다.
  - 기본 체인은 공통 모듈에서 만들고, habit-service만 guest filter를 추가하는 확장 지점을 둡니다.
  - `ROLE_INTERNAL_API` 문자열도 공통 상수만 사용하게 정리합니다.

### 6. InternalRestClient 요청 생성 중복과 오류 은닉 개선

- 근거: `InternalRestClient.exchange()`와 `tryExchange()`가 거의 같은 요청 조립 코드를 반복합니다. 관련 코드: `modules/security/src/main/java/com/dochiri/security/internalapi/InternalRestClient.java:28`, `:54`.
- 문제:
  - `tryExchange()`는 모든 `RestClientException`을 `Optional.empty()`로 숨겨 인증 실패, 네트워크 장애, 5xx를 구분할 수 없습니다.
  - 타임아웃, retry, circuit breaker, correlation id 전파가 없습니다.
- 제안:
  - 요청 생성 로직을 private method로 분리합니다.
  - `tryExchange()`도 401/404처럼 의도된 실패만 empty로 처리하고, 5xx/네트워크 장애는 로깅 또는 도메인 에러로 구분합니다.
  - `RestClient.Builder` bean을 받아 connect/read timeout, observation, 공통 헤더를 적용합니다.

### 7. 컨트롤러의 DTO 변환 반복 정리

- 근거: `HabitController`가 모든 endpoint에서 `owner()` 호출, command 생성, response 변환을 반복합니다.
- 문제: 컨트롤러가 유스케이스 라우팅 외에 인증 주체 해석과 변환 코드를 많이 갖습니다.
- 제안:
  - `@CurrentHabitOwner` 같은 argument resolver를 도입해 `HabitOwner`를 파라미터로 주입합니다.
  - command 생성은 request DTO 또는 작은 mapper로 일관되게 모읍니다.
  - CRUD 응답 status도 생성은 `201 Created`, 삭제는 `204 No Content`처럼 REST 관례에 맞춥니다.

## P2: habit-service 성능/정합성

### 8. 습관 순서 교환 로직의 임시 인덱스 제거

- 근거: `SwapHabitIndexService`는 `Integer.MAX_VALUE`를 임시 parking index로 사용해 세 번 저장합니다. 관련 코드: `habit-service/src/main/java/com/dochiri/habitservice/application/service/SwapHabitIndexService.java:21`, `:38`.
- 위험: `Integer.MAX_VALUE`가 실제 인덱스와 충돌할 가능성이 있고, unique constraint를 추가하기 어렵습니다.
- 제안:
  - DB에서 `(owner_type, owner_public_id, sort_index)` unique constraint를 명확히 둡니다.
  - repository에 `swapIndex(owner, sourceId, targetId)` 같은 명령형 메서드를 두고 단일 트랜잭션 안에서 lock 또는 bulk update로 처리합니다.
  - 같은 habit id가 들어온 경우를 명시적으로 no-op 또는 validation error로 처리합니다.

### 9. 잔디 집계를 DB 집계로 밀어내기

- 근거: `HabitRecordJpaAdapter.aggregateGrassByOwnerAndCompletedDateBetween()`는 DB에서 record entity 목록을 가져온 뒤 Java에서 grouping/sum을 수행합니다. 관련 코드: `habit-service/src/main/java/com/dochiri/habitservice/infrastructure/adapter/out/persistence/record/HabitRecordJpaAdapter.java:82`.
- 문제: 기간이 길고 기록이 많아지면 불필요한 entity materialization이 발생합니다.
- 제안:
  - QueryDSL projection으로 `completedDate`, `count`, `sum(durationMinutes)`만 조회합니다.
  - `HabitGrassAggregation` 전용 projection DTO를 repository custom query에서 반환합니다.
  - `completed_date`, `habit_id` 복합 인덱스와 habit owner 조회 인덱스를 점검합니다.

### 10. 첫 습관 생성일 조회 최적화

- 근거: `GetHabitGrassService.firstHabitCreatedDate()`가 owner의 모든 habit을 가져온 뒤 stream에서 최소 생성일을 계산합니다. 관련 코드: `habit-service/src/main/java/com/dochiri/habitservice/application/service/GetHabitGrassService.java:61`.
- 제안:
  - `HabitRepository`에 `findFirstCreatedAtByOwner()`를 추가해 DB에서 `min(created_at)`만 조회합니다.
  - 잔디 조회는 `minCreatedAt`와 aggregate query 두 개만 호출하도록 단순화합니다.

## P3: 설정/빌드 정리

### 11. Gradle 의존성 중복 제거

- 근거: `auth-service`, `habit-service`, `user-service`가 web, validation, data-jpa, config, eureka, swagger, jjwt, lombok, test 의존성을 거의 반복합니다.
- 제안:
  - root `build.gradle`에 서비스 공통 convention을 만들거나 `buildSrc`/convention plugin으로 분리합니다.
  - `modules:security`가 JWT API를 노출한다면 개별 서비스의 `io.jsonwebtoken:jjwt-api` 직접 의존이 정말 필요한지 재검토합니다.
  - QueryDSL annotation processor 설정도 JPA 사용 서비스에 공통 적용되게 정리합니다.

### 12. 사용하지 않는 Kafka 모듈의 책임 재확인

- 근거: `modules:kafka`와 docker compose의 Kafka broker가 존재하지만, 현재 주요 서비스 코드에서 Kafka publisher/consumer 사용은 보이지 않습니다.
- 제안:
  - 가까운 계획에 없으면 compose 기본 실행에서 Kafka를 제외하고 별도 profile로 분리합니다.
  - 사용할 계획이면 auth/user/habit 간 이벤트 경계를 문서화하고, 이벤트 스키마 모듈을 따로 둡니다.

### 13. 운영 설정과 로컬 설정 분리

- 근거: `gateway/src/main/resources/application.yml:73`은 JWT secret 기본값을 갖고 있고, docker compose에도 여러 기본 비밀번호가 있습니다.
- 제안:
  - 운영 profile에서는 secret/password 기본값을 제거합니다.
  - `application-local.yml`, `application-prod.yml`로 의도를 분리합니다.
  - README의 Redis 설명은 “리프레시 토큰 저장”에서 “인증 세션/refresh token id 기반 session 저장”처럼 현재 구현에 맞게 갱신합니다.

## P4: 테스트 보강

### 14. 통합 시나리오 테스트 추가

현재 단위 테스트는 꽤 있지만, 서비스 경계와 Redis/내부 API 흐름의 회귀를 잡는 테스트가 더 필요합니다.

우선 추가할 테스트:

- 카카오 로그인 중 user-service 생성 성공 후 auth 저장 실패 재시도 시 idempotency 보장
- refresh token 재발급 시 기존 session 교체와 gateway session 확인 일치
- guest session으로 생성한 habit을 로그인 후 user owner로 migration
- 내부 API 토큰 누락/불일치 시 `/internal/**` 접근 차단
- habit record 중복 날짜 생성 시 도메인 에러 응답
- 잔디 조회에서 `from > to`, 미래 날짜, 첫 습관 생성일 이전 요청 처리
- 같은 habit id로 index swap 요청

## 권장 작업 순서

1. `INTERNAL_API_TOKEN`, `JWT_SECRET`, config/eureka 기본 계정의 profile별 설정 정리
2. `modules/security`에 공통 SecurityFilterChain builder와 JWT 검증 컴포넌트 추출
3. `InternalRestClient` 리팩토링 및 타임아웃/오류 분류 추가
4. `AuthSessionRedisAdapter` 원자 저장 방식 개선
5. `SocialAccountProvisioner` 트랜잭션 경계 축소
6. habit-service 잔디 집계와 첫 생성일 조회를 DB projection으로 변경
7. `SwapHabitIndexService`를 repository 명령으로 내리고 unique constraint 추가
8. 위 변경을 보호하는 통합 테스트 작성

