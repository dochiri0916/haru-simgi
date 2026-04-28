# Frontend API Specification

이 문서는 현재 구현 코드를 기준으로 프론트엔드에서 연동해야 하는 API를 정리한다.

## Base URL

- 로컬 게이트웨이 기본값: `http://localhost:8080`
- 게이트웨이 라우팅:
  - `/api/auth/**` -> `auth-service`
  - `/api/users/**` -> `user-service`
  - `/api/habits/**` -> `habit-service`

## 인증 방식

### 로그인 사용자

로그인/토큰 재발급 응답은 항상 JSON body에 토큰을 포함한다.

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "refreshTokenExpiresAt": "2026-05-13T00:00:00Z",
  "role": "USER",
  "guestMerge": null
}
```

프론트는 두 방식 중 하나를 선택할 수 있다.

1. Bearer 토큰 방식
   - 요청 헤더: `Authorization: Bearer {accessToken}`
   - 로그인/재발급 요청 시 `X-Auth-Transport: bearer`를 보내면 서버가 인증 쿠키를 내려주지 않는다.
2. HttpOnly 쿠키 방식
   - `X-Auth-Transport`를 생략하면 기본값은 cookie다.
   - 서버가 `access_token`, `refresh_token` 쿠키를 `Set-Cookie`로 내려준다.
   - fetch/axios 요청에는 credentials 포함이 필요하다.

### 게스트 사용자

게스트는 `guest_session` HttpOnly 쿠키로 식별된다.

- 프론트는 쿠키 값을 읽지 않는다.
- 게스트 API 호출에는 credentials 포함이 필요하다.
- 습관 API는 JWT가 있으면 로그인 사용자로 처리하고, JWT가 없고 유효한 `guest_session` 쿠키가 있으면 게스트 사용자로 처리한다.
- JWT와 `guest_session`이 동시에 있으면 JWT가 우선된다.

## 공통 오류 응답

서비스 내부 오류는 대체로 `application/problem+json` 형태다.

```json
{
  "type": "/errors/invalid-guest-session",
  "title": "INVALID_GUEST_SESSION",
  "status": 401,
  "detail": "유효하지 않은 게스트 세션입니다.",
  "code": "INVALID_GUEST_SESSION"
}
```

게이트웨이의 `/api/users/**` 인증 실패는 별도 JSON 형태다.

```json
{
  "status": 401,
  "title": "Unauthorized",
  "detail": "인증이 필요합니다."
}
```

## Auth API

### 카카오 인가 URL 조회

```http
GET /api/auth/login/kakao/authorize?state={state}
```

응답 `200`

```json
{
  "authorizeUrl": "https://kauth.kakao.com/oauth/authorize?..."
}
```

### 게스트 세션 발급

```http
POST /api/auth/guest
```

응답 `200`

```json
{
  "guestId": "guest-public-id",
  "status": "ACTIVE",
  "expiresAt": "2026-07-28T00:00:00Z"
}
```

응답 헤더:

```http
Set-Cookie: guest_session=...; HttpOnly; Path=/; SameSite=Lax; Max-Age=7776000
```

프론트 권장 동작:

- 앱 초기화 시 로그인 토큰이 없고 게스트 쿠키 존재 여부를 알 수 없으면 먼저 `GET /api/auth/guest/me`를 호출한다.
- `401 INVALID_GUEST_SESSION`이거나 쿠키가 없으면 `POST /api/auth/guest`를 호출한다.

### 게스트 세션 확인

```http
GET /api/auth/guest/me
Cookie: guest_session=...
```

응답 `200`

```json
{
  "guestId": "guest-public-id",
  "status": "ACTIVE",
  "expiresAt": "2026-07-28T00:00:00Z"
}
```

`status` 값:

- `ACTIVE`
- `LINKED`
- `EXPIRED`
- `REVOKED`

현재 habit-service는 `ACTIVE`인 게스트 세션만 인증으로 인정한다.

### 카카오 로그인

```http
POST /api/auth/login/kakao
Content-Type: application/json
X-Auth-Transport: bearer
Cookie: guest_session=...

{
  "code": "kakao-authorization-code"
}
```

응답 `200`

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "refreshTokenExpiresAt": "2026-05-13T00:00:00Z",
  "role": "USER",
  "guestMerge": "SUCCEEDED"
}
```

동작:

- `guest_session` 쿠키가 있고 유효하면 로그인 중 게스트 습관을 로그인 사용자로 이전한다.
- 이전이 성공하면 게스트 세션은 `LINKED`가 되고 응답에서 `guest_session` 쿠키를 삭제한다.
- `guestMerge` 값은 `SKIPPED`, `SUCCEEDED`, `FAILED` 중 하나다.
- 현재 구현은 `PLAN.md`의 객체 형태가 아니라 문자열 enum으로 `guestMerge`를 응답한다.
- habit-service 이전 호출이 실패해도 로그인 토큰은 발급되고 `guestMerge: "FAILED"`가 응답된다.

### 카카오 콜백 로그인

```http
GET /api/auth/login/kakao/callback?code={code}
Cookie: guest_session=...
```

응답:

- 성공 시 `302`
- `Location`은 서버 설정의 frontend redirect URI
- cookie transport 기준으로 인증 쿠키를 내려준다.

SPA에서 code를 직접 받아 처리하는 구조라면 `POST /api/auth/login/kakao` 사용을 권장한다.

### 토큰 재발급

Bearer 방식:

```http
POST /api/auth/refresh
Content-Type: application/json
X-Auth-Transport: bearer

{
  "refreshToken": "..."
}
```

쿠키 방식:

```http
POST /api/auth/refresh
Cookie: refresh_token=...
```

응답 `200`

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "refreshTokenExpiresAt": "2026-05-13T00:00:00Z",
  "role": "USER",
  "guestMerge": null
}
```

### 로그아웃

Bearer 방식:

```http
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "..."
}
```

쿠키 방식:

```http
POST /api/auth/logout
Cookie: refresh_token=...
```

응답 `204`

쿠키 방식이면 `access_token`, `refresh_token` 삭제 쿠키가 내려온다.

## User API

### 현재 사용자 조회

```http
GET /api/users/me
Authorization: Bearer {accessToken}
```

응답 `200`

```json
{
  "id": "user-public-id",
  "nickname": "닉네임",
  "profileImageUrl": "https://..."
}
```

주의:

- `/api/users/**`는 게이트웨이에서 로그인 인증이 필수다.
- 게스트 세션으로는 호출할 수 없다.

## Habit API

모든 `/api/habits/**` API는 아래 둘 중 하나가 필요하다.

- 로그인 사용자: `Authorization: Bearer {accessToken}` 또는 `access_token` 쿠키
- 게스트 사용자: `guest_session` 쿠키

### 습관 목록 조회

```http
GET /api/habits
```

응답 `200`

```json
{
  "habits": [
    {
      "id": "habit-id",
      "name": "매일 운동하기",
      "color": "GREEN",
      "colorHex": "#10b981",
      "index": 0,
      "createdAt": "2026-04-29T00:00:00Z"
    }
  ]
}
```

### 습관 생성

```http
POST /api/habits
Content-Type: application/json

{
  "name": "매일 운동하기",
  "color": "GREEN"
}
```

요청 필드:

- `name`: 필수, 최대 50자
- `color`: 선택 가능 값 `BLUE`, `GREEN`, `RED`, `YELLOW`, `PURPLE`, `PINK`

응답 `200`

```json
{
  "id": "habit-id",
  "name": "매일 운동하기",
  "color": "GREEN",
  "colorHex": "#10b981",
  "index": 0,
  "createdAt": "2026-04-29T00:00:00Z"
}
```

### 습관 상세 조회

```http
GET /api/habits/{habitId}
```

응답 `200`

```json
{
  "id": "habit-id",
  "name": "매일 운동하기",
  "color": "GREEN",
  "colorHex": "#10b981",
  "index": 0,
  "createdAt": "2026-04-29T00:00:00Z"
}
```

### 습관 이름 수정

```http
PATCH /api/habits/{habitId}
Content-Type: application/json

{
  "name": "매일 독서하기"
}
```

응답 `200`

```json
{
  "id": "habit-id",
  "name": "매일 독서하기",
  "color": "GREEN",
  "colorHex": "#10b981",
  "index": 0,
  "createdAt": "2026-04-29T00:00:00Z"
}
```

### 습관 정렬 순서 교환

```http
PATCH /api/habits/index/swap
Content-Type: application/json

{
  "sourceHabitId": "habit-id-1",
  "targetHabitId": "habit-id-2"
}
```

응답 `200`

```json
{
  "habits": [
    {
      "id": "habit-id-1",
      "name": "매일 운동하기",
      "color": "GREEN",
      "colorHex": "#10b981",
      "index": 1,
      "createdAt": "2026-04-29T00:00:00Z"
    },
    {
      "id": "habit-id-2",
      "name": "매일 독서하기",
      "color": "BLUE",
      "colorHex": "#3b82f6",
      "index": 0,
      "createdAt": "2026-04-29T00:00:00Z"
    }
  ]
}
```

### 습관 삭제

```http
DELETE /api/habits/{habitId}
```

응답 `204`

### 습관 기록 조회

```http
GET /api/habits/{habitId}/records?from=2026-04-01&to=2026-04-30
```

쿼리:

- `from`: 선택, `YYYY-MM-DD`
- `to`: 선택, `YYYY-MM-DD`

응답 `200`

```json
{
  "habitId": "habit-id",
  "records": [
    {
      "id": "record-id",
      "completedAt": "2026-04-29T09:00:00Z",
      "minutes": 30,
      "level": 2,
      "memo": "클린 아키텍처"
    }
  ]
}
```

### 습관 기록 생성

```http
POST /api/habits/{habitId}/records
Content-Type: application/json

{
  "completedAt": "2026-04-29T09:00:00Z",
  "minutes": 30,
  "memo": "클린 아키텍처"
}
```

요청 필드:

- `completedAt`: 선택, ISO 8601 instant
- `minutes`: 선택, 분 단위
- `memo`: 선택, 최대 200자

응답 `200`

```json
{
  "id": "record-id",
  "habitId": "habit-id",
  "completedAt": "2026-04-29T09:00:00Z",
  "minutes": 30,
  "level": 2,
  "memo": "클린 아키텍처"
}
```

### 습관 기록 수정

```http
PATCH /api/habits/{habitId}/records/{recordId}
Content-Type: application/json

{
  "completedAt": "2026-04-29T10:00:00Z",
  "minutes": 45,
  "memo": null
}
```

요청 필드:

- 생략한 필드는 기존 값을 유지한다.
- `memo: null`을 보내면 메모를 삭제한다.
- `minutes`: 0 이상 1440 이하

응답 `200`

```json
{
  "id": "record-id",
  "habitId": "habit-id",
  "completedAt": "2026-04-29T10:00:00Z",
  "minutes": 45,
  "level": 3,
  "memo": null
}
```

### 습관 기록 삭제

```http
DELETE /api/habits/{habitId}/records/{recordId}
```

응답 `204`

### 잔디 조회

```http
GET /api/habits/grass?from=2026-04-01&to=2026-04-30
```

쿼리:

- `from`: 선택, `YYYY-MM-DD`
- `to`: 선택, `YYYY-MM-DD`

응답 `200`

```json
{
  "fromDate": "2026-04-01",
  "toDate": "2026-04-30",
  "totalValue": 12,
  "days": [
    {
      "date": "2026-04-29",
      "value": 2,
      "level": 2
    }
  ]
}
```

레벨 기준:

- `0건 -> 0`
- `1건 -> 1`
- `2건 -> 2`
- `3~4건 -> 3`
- `5건 이상 -> 4`

## 프론트 적용 플로우

### 첫 방문 또는 비로그인 진입

1. 로그인 토큰이 없으면 `GET /api/auth/guest/me`를 호출한다.
2. 실패하면 `POST /api/auth/guest`를 호출한다.
3. 이후 `/api/habits/**` 요청은 credentials를 포함해서 호출한다.

### 게스트가 카카오 로그인하는 경우

1. 카카오 인가 코드를 받는다.
2. `POST /api/auth/login/kakao`를 credentials 포함으로 호출한다.
3. 응답 토큰을 저장하거나, cookie transport라면 인증 쿠키를 사용한다.
4. `guestMerge`를 확인한다.
5. 로그인 후 `/api/habits`를 다시 조회한다.

`guestMerge` 처리:

- `SUCCEEDED`: 게스트 데이터 이전 완료. 서버가 `guest_session` 쿠키를 삭제한다.
- `SKIPPED`: 이전할 활성 게스트 세션이 없음. 게스트 쿠키가 있었다면 서버가 삭제할 수 있다.
- `FAILED`: 로그인은 성공했지만 게스트 데이터 이전 실패. 현재 별도 retry API는 없다.

### 토큰 만료

1. `/api/users/me` 또는 로그인 전용 API에서 `401`이 오면 `POST /api/auth/refresh`를 호출한다.
2. 재발급 성공 시 실패한 요청을 재시도한다.
3. 재발급 실패 시 로그인 상태를 해제한다.
4. 비로그인 사용을 계속 지원하려면 `POST /api/auth/guest`로 새 게스트 세션을 발급한다.

## PLAN.md 대비 현재 구현 차이

- 카카오 로그인 응답의 `guestMerge`는 PLAN의 객체가 아니라 문자열 enum이다.
- 게스트 습관 이전 내부 API는 `POST /internal/habits/owners/merge`가 아니라 `PATCH /internal/habits/guest-owner`로 구현되어 있다.
- 이전 실패 시 로그인은 성공하고 `guestMerge: "FAILED"`를 응답한다.
- retry API는 아직 구현되어 있지 않다.
- `guest_merge_jobs` 상태 테이블과 비동기/재시도 상태 응답은 현재 구현되어 있지 않다.
