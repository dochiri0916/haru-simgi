# 비로그인 사용 및 소셜 연동 고도화 계획

## 전제

MVP 출시는 끝났고, 이제는 빠른 도입보다 사용자 데이터 보존, 보안 경계, 장애 복구, 운영 가시성을 우선한다. 모바일 앱은 출시하지 않으므로 게스트 식별자는 웹 브라우저 쿠키 기반으로 설계한다.

## 목표

- 사용자는 로그인 없이도 바로 습관을 만들고 기록할 수 있다.
- 비로그인 데이터는 서버가 관리하는 게스트 세션에 귀속된다.
- 사용자가 소셜 계정을 연동하면 게스트 데이터를 로그인 사용자 데이터로 안전하게 이전한다.
- 이전 과정은 중복 호출, 부분 실패, 재시도에 견딘다.
- 장기 운영을 위해 만료, 정리, 감사 로그, 관측 지표를 갖춘다.

## 권장 결정

- 게스트 식별자 전달: `HttpOnly Secure SameSite=Lax` 쿠키만 사용
- 게스트 세션 저장: 서버 저장소 사용
- 게스트 데이터 보관: 90일 미사용 만료
- 소셜 연동 실패 정책: 로그인은 성공시키되, 데이터 이전은 재시도 가능한 상태로 남김
- 습관 병합 정책: 자동 병합하지 않고 사용자 기존 습관 뒤에 append
- 내부 서비스 간 이전 요청: 멱등키를 가진 내부 API + 상태 테이블로 관리

## 현재 구조 요약

- `auth-service`는 카카오 로그인 후 `user-service`에 사용자를 생성하고 `auth_users.publicId` 기준으로 JWT를 발급한다.
- `habit-service` 도메인에는 이미 `HabitOwner.user(...)`, `HabitOwner.guest(...)`, `OwnerType.USER/GUEST`가 있다.
- 현재 `HabitController`는 `JwtPrincipal.publicId()`만 사용하고, 서비스는 `HabitOwner.user(...)`만 호출한다.
- 따라서 `GUEST` 모델은 존재하지만 외부 API와 연결되지 않았다.

## 설계 원칙

### 게스트는 계정이 아니다

게스트를 `users`나 `auth_users`에 미리 만들지 않는다. 게스트는 habit-service 데이터의 임시 owner다. 소셜 연동 시점에만 user/auth 계정을 만들고, habit 데이터의 owner를 `GUEST`에서 `USER`로 이전한다.

### 쿠키는 식별자, 서버 세션이 권한 기준

브라우저는 `guest_session` 쿠키만 가진다. 쿠키 값은 추측 불가능한 랜덤 토큰이며, 서버는 해시된 토큰을 `guest_sessions`에 저장한다. 요청 시 쿠키 토큰을 해시해 활성 세션인지 검증한다.

### 데이터 이전은 이벤트가 아니라 상태 있는 작업이다

소셜 연동은 여러 서비스가 얽히므로 “한 번 호출하고 끝”으로 두면 운영 중 복구가 어렵다. `guest_link_attempts` 또는 `guest_merge_jobs` 같은 상태 테이블을 두고 `PENDING/RUNNING/SUCCEEDED/FAILED`를 추적한다.

## 도메인 모델

### guest_sessions

`auth-service` 또는 별도 session 책임 모듈에서 관리한다. 현재 구조에서는 `auth-service`가 가장 자연스럽다.

필드 초안:

- `id`: 내부 PK
- `public_id`: 외부 노출용 세션 ID
- `token_hash`: 쿠키 토큰 해시, unique
- `status`: `ACTIVE`, `LINKED`, `EXPIRED`, `REVOKED`
- `created_at`
- `last_seen_at`
- `expires_at`
- `linked_user_public_id`: 연동 완료 시 사용자 publicId
- `linked_at`

쿠키에는 `public_id`가 아니라 랜덤 토큰을 넣는다. DB에는 원문 토큰을 저장하지 않는다.

### habit owner

현재 구조를 살린다.

- 로그인 사용자: `OwnerType.USER + userPublicId`
- 게스트 사용자: `OwnerType.GUEST + guestPublicId`

장기적으로 `owner_public_id` 컬럼명은 `owner_id`로 바꾸는 것이 정확하지만, 기능 구현과 분리해서 별도 마이그레이션으로 처리한다.

### guest_merge_jobs

habit-service 또는 auth-service에 둘 수 있다. 권장 위치는 habit-service다. 실제 데이터 이전의 원자성과 상태를 habit-service가 가장 잘 보장할 수 있기 때문이다.

필드 초안:

- `id`
- `idempotency_key`: `guestPublicId:userPublicId` 또는 auth-service가 발급한 UUID
- `guest_public_id`
- `user_public_id`
- `status`: `PENDING`, `RUNNING`, `SUCCEEDED`, `FAILED`
- `failure_reason`
- `requested_at`
- `started_at`
- `finished_at`

## API 설계

### 게스트 세션 시작

```http
POST /api/auth/guest
```

응답:

```json
{
  "guestId": "guest-public-id",
  "expiresAt": "2026-07-26T00:00:00Z"
}
```

서버 동작:

- 랜덤 토큰 생성
- `guest_sessions`에 해시 저장
- `Set-Cookie: guest_session=...; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=7776000`

### 게스트 세션 확인

```http
GET /api/auth/guest/me
```

응답:

```json
{
  "guestId": "guest-public-id",
  "status": "ACTIVE",
  "expiresAt": "2026-07-26T00:00:00Z"
}
```

프론트 초기화 시 로그인 토큰이 없으면 이 API로 현재 게스트 세션을 확인한다.

### habit API

기존 URL은 유지한다.

```http
GET /api/habits
Cookie: guest_session=...
```

또는

```http
GET /api/habits
Authorization: Bearer ...
```

해석 우선순위:

1. 유효한 JWT가 있으면 `OwnerType.USER`
2. JWT가 없고 유효한 guest session 쿠키가 있으면 `OwnerType.GUEST`
3. 둘 다 없으면 `401 UNAUTHORIZED`

JWT와 guest 쿠키가 동시에 있으면 JWT를 우선한다. 단, 소셜 연동 직후 이전이 끝날 때까지는 auth-service가 guest 쿠키를 바로 삭제하지 않고 merge 결과를 확인한 뒤 삭제한다.

### 카카오 로그인 + 게스트 연동

```http
POST /api/auth/login/kakao
Cookie: guest_session=...
Content-Type: application/json

{
  "code": "..."
}
```

응답:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "role": "USER",
  "guestMerge": {
    "status": "SUCCEEDED",
    "retryable": false
  }
}
```

`guestMerge.status` 값:

- `SKIPPED`: 게스트 세션 없음
- `SUCCEEDED`: 이전 완료
- `PENDING`: 이전 요청 등록, 아직 처리 중
- `FAILED`: 이전 실패, 재시도 가능

### 내부 데이터 이전 API

```http
POST /internal/habits/owners/merge
X-Internal-Api-Token: ...
Idempotency-Key: ...
Content-Type: application/json

{
  "guestId": "guest-public-id",
  "userPublicId": "user-public-id"
}
```

응답:

```json
{
  "status": "SUCCEEDED",
  "movedHabits": 5,
  "movedRecords": 43
}
```

## 서비스별 변경 계획

### auth-service

- `GuestSession` 도메인과 저장소 추가
- `POST /api/auth/guest`, `GET /api/auth/guest/me` 추가
- guest cookie 발급/검증/삭제 컴포넌트 추가
- 카카오 로그인 시 guest cookie가 있으면 세션 검증
- 로그인 성공 후 habit-service merge 내부 API 호출
- merge 성공 시 guest session을 `LINKED`로 변경하고 쿠키 삭제
- merge 실패 시 session은 `ACTIVE` 또는 `LINK_PENDING` 상태로 유지하고 재시도 가능하게 둠
- 응답에 `guestMerge` 상태 포함

### habit-service

- `OwnerContext` 도입
  - `OwnerType type`
  - `String ownerId`
- 모든 habit command를 `ownerPublicId` 중심에서 `OwnerContext` 중심으로 변경
- 컨트롤러에 `OwnerContextArgumentResolver` 추가
  - JWT가 있으면 USER
  - 없으면 auth-service guest 검증 결과 또는 gateway 전달 헤더를 기반으로 GUEST
- 내부 merge API 추가
- merge 트랜잭션 구현
  - 게스트 습관 조회
  - 사용자 기존 최대 sort index 조회
  - 게스트 습관 owner를 USER로 변경
  - sort index 재배치
  - 기록은 habit FK를 유지하므로 습관 owner 이전으로 함께 따라감
- `guest_merge_jobs`로 멱등성과 실패 이력 관리

### user-service

- 게스트 단계에서는 변경하지 않는다.
- 소셜 연동 시 현재처럼 사용자 생성/조회만 담당한다.
- 이미 존재하는 사용자가 로그인하는 경우에도 `publicId`를 안정적으로 반환해야 한다.
- 게스트 merge는 user-service 책임이 아니다.

### gateway

- 웹 쿠키가 각 서비스까지 안정적으로 전달되는지 확인한다.
- 가능하면 gateway에서 guest session을 검증해 downstream에 `X-Owner-Type`, `X-Owner-Id`를 전달하는 방식도 고려한다.
- 다만 초기 구현은 각 서비스 직접 검증보다 gateway 책임이 커진다. 현재 서비스 수가 적으므로 habit-service에서 필요한 만큼만 검증하는 편이 단순하다.

## 병합 정책

### 기본 정책

- 게스트 습관은 사용자 기존 습관 뒤에 append한다.
- 자동 중복 병합은 하지 않는다.
- 기록은 습관 단위로 그대로 유지한다.
- 동일한 merge 요청은 같은 결과를 반환한다.

### 왜 자동 병합하지 않는가

습관 이름이 같아도 의미가 다를 수 있다. 예를 들어 `운동`이라는 이름이 같아도 하나는 유산소, 다른 하나는 근력일 수 있다. 자동 병합은 되돌리기 어려운 데이터 변형이므로 출시 후 다듬는 단계에서는 피한다.

### 충돌 처리

- 사용자 기존 habit `sort_index` 최대값을 기준으로 게스트 습관 index를 다시 부여한다.
- publicId unique 충돌은 발생하지 않아야 한다. 발생하면 merge를 실패 처리하고 운영 로그를 남긴다.
- merge 중 실패하면 전체 트랜잭션을 rollback한다.

## 실패 및 복구 정책

### merge 실패 시

- 로그인은 성공시킨다.
- guest cookie는 삭제하지 않는다.
- `guestMerge.status=FAILED`, `retryable=true`를 응답한다.
- 클라이언트는 “데이터 이전 재시도”를 조용히 재호출할 수 있다.
- 서버는 실패 사유와 idempotency key를 로그에 남긴다.

### 재시도 API

```http
POST /api/auth/guest/merge/retry
Authorization: Bearer ...
Cookie: guest_session=...
```

서버는 현재 로그인 사용자와 guest session을 검증한 뒤 같은 내부 merge API를 재호출한다.

### 운영자 복구

- `guest_merge_jobs`에서 `FAILED` 상태를 조회할 수 있어야 한다.
- 실패 job을 같은 idempotency key로 재실행할 수 있어야 한다.
- 실패율, 평균 처리 시간, 이동된 습관/기록 수를 지표로 남긴다.

## 만료 및 정리 정책

- guest session 기본 만료: 90일
- guest session은 활동 시 `last_seen_at`, `expires_at`를 연장한다.
- `EXPIRED` 또는 90일 이상 미사용 guest owner 데이터는 정리 대상이다.
- 실제 삭제는 하드 삭제보다 1차 soft delete 또는 보관 테이블 이동을 먼저 고려한다.
- 정리 배치는 다음 순서로 둔다.
  1. 만료된 guest session을 `EXPIRED` 처리
  2. 만료 후 유예 기간 14일 경과한 guest habit/record 삭제
  3. 삭제 건수와 소요 시간을 로그/지표로 남김

## 보안 고려사항

- guest cookie는 `HttpOnly`, `Secure`, `SameSite=Lax`, `Path=/`를 기본으로 한다.
- 로컬 개발에서는 `Secure=false` 프로파일을 별도 제공한다.
- 쿠키 토큰 원문은 DB에 저장하지 않는다.
- guest session 검증 실패와 부재는 같은 응답으로 처리해 유효한 세션 존재 여부를 노출하지 않는다.
- 소셜 연동 merge는 내부 API 토큰과 idempotency key를 모두 요구한다.
- guest session이 `LINKED`, `EXPIRED`, `REVOKED` 상태면 habit API 접근을 허용하지 않는다.

## 프론트엔드 흐름

### 첫 진입

1. `/api/users/me` 또는 auth 상태 확인
2. 로그인 상태가 아니면 `/api/auth/guest/me`
3. guest session이 없으면 `/api/auth/guest`
4. 이후 habit API를 그대로 호출

### 소셜 연동

1. 사용자가 “카카오로 백업/연동” 클릭
2. 기존 guest cookie를 유지한 채 카카오 로그인 진행
3. 로그인 응답의 `guestMerge.status` 확인
4. `SUCCEEDED`면 로그인 사용자 화면으로 전환
5. `FAILED` 또는 `PENDING`이면 데이터 이전 상태를 화면에 작게 표시하고 재시도 가능하게 둠

### 로그아웃

- 로그인 토큰만 제거한다.
- 이미 연동 완료된 guest cookie는 서버가 삭제했어야 한다.
- 사용자가 로그아웃 후 다시 비로그인으로 쓰면 새 guest session을 발급한다.

## 테스트 계획

### 단위 테스트

- guest session 생성 시 token hash만 저장
- 만료된 guest session 검증 실패
- `OwnerContext`가 USER/GUEST를 정확히 `HabitOwner`로 변환
- merge 시 sort index 재배치
- 동일 idempotency key 재호출 시 중복 이전 없음

### 통합 테스트

- 게스트 세션 발급 후 habit 생성/조회
- JWT가 있으면 guest cookie가 있어도 USER owner 사용
- 카카오 로그인 성공 + 게스트 데이터 이전 성공
- 카카오 로그인 성공 + habit-service merge 실패 시 로그인 성공과 retryable 응답
- retry API로 실패한 merge 재시도 성공
- 만료된 guest session으로 habit API 호출 시 401

### 회귀 테스트

- 기존 로그인 사용자 habit API 동작 유지
- 기존 카카오 로그인/토큰 재발급/로그아웃 동작 유지
- 기존 사용자 데이터가 게스트 merge로 삭제되지 않음

## 구현 순서

1. `PLAN.md` 기준 API/상태/쿠키 이름 확정
2. `auth-service`에 guest session 도메인, 저장소, 쿠키 매니저 추가
3. `habit-service`에 `OwnerContext`와 command 일반화 적용
4. habit API에서 guest owner 접근 허용
5. habit 내부 merge API와 `guest_merge_jobs` 구현
6. 카카오 로그인 후 merge 호출 연결
7. merge retry API와 실패 상태 응답 추가
8. 만료/정리 배치 추가
9. Swagger와 운영 문서 정리
10. 통합 테스트와 회귀 테스트 보강

## 리스크와 대응

- 쿠키 도메인/SameSite 설정 오류: 로컬, 스테이징, 운영 프로파일별 쿠키 설정을 분리한다.
- merge 중 부분 이전: habit-service 단일 트랜잭션으로 처리하고 job 상태를 남긴다.
- 중복 merge: idempotency key와 `guest_merge_jobs` unique 제약으로 막는다.
- 게스트 데이터 방치: 90일 만료와 정리 배치를 운영한다.
- 로그인은 됐는데 데이터가 안 보이는 상태: `guestMerge` 응답과 retry API로 복구 경로를 제공한다.

## 최종 방향

출시 후 다듬는 단계에서는 “게스트 ID만 쿠키에 넣고 끝”보다 서버 저장 guest session과 상태 있는 merge job을 두는 편이 맞다. 구현량은 늘지만, 사용자의 습관 기록은 제품의 핵심 데이터이므로 소셜 연동 과정에서 잃거나 복구 불가능한 상태로 두면 안 된다. 현재 코드에는 이미 `OwnerType.GUEST`의 기반이 있으므로, 핵심 작업은 계정 모델 확장이 아니라 owner 해석, guest session 검증, 멱등한 소유권 이전을 완성하는 것이다.
