# Frontend API Spec

웹 프론트엔드 기준 API 명세다. 현재 서비스는 웹 우선이므로 기본 인증 방식은 `HttpOnly Cookie`다. 프론트는 토큰을 직접 저장하지 않고, 브라우저 쿠키를 사용해 인증 상태를 유지하면 된다.

## 기본 정보

- Base URL: `http://localhost:8080`
- 공통 Prefix: `/api`
- Content-Type: `application/json`
- 웹 클라이언트 요청 설정:
  - `fetch`: `credentials: 'include'`
  - `axios`: `withCredentials: true`

## 인증 방식

웹에서는 아래 방식으로 동작한다.

- 로그인 성공 시 서버가 `access_token`, `refresh_token` 쿠키를 내려준다
  - 두 쿠키 모두 `Path=/`로 설정되어 모든 API 경로에서 자동으로 전송됨
  - `HttpOnly` 속성으로 JavaScript에서 접근 불가 (XSS 방지)
- 이후 보호 API는 쿠키로 자동 인증된다
- 프론트는 앱 초기 진입 시 `GET /api/users/me` 호출로 로그인 상태를 확인하면 된다

참고:

- `X-Auth-Transport: bearer`는 앱 클라이언트용이다
- 웹 프론트에서는 기본값을 그대로 사용하면 되므로 이 헤더를 보낼 필요가 없다
- fetch나 axios 사용 시 `credentials: 'include'` / `withCredentials: true` 설정이 필수

## 에러 응답 형식

서버는 Spring `ProblemDetail` 기반 JSON을 반환한다.

예시:

```json
{
  "type": "/errors/invalid-refresh-token",
  "title": "INVALID_REFRESH_TOKEN",
  "status": 401,
  "detail": "유효하지 않은 리프레시 토큰입니다.",
  "instance": "/api/auth/refresh",
  "code": "INVALID_REFRESH_TOKEN"
}
```

프론트 권장 처리:

- `401`: 로그인 만료로 간주하고 재발급 시도 또는 로그인 페이지 이동
- `403`: 권한 없음
- `400`: 사용자 입력 오류
- `500`: 공통 에러 토스트 또는 에러 화면

## 인증 API

### 1. 카카오 로그인 URL 조회

- Method: `GET`
- Path: `/api/auth/login/kakao/authorize`
- Query:
  - `state?: string`
- Auth: 불필요

응답:

```json
{
  "authorizeUrl": "https://kauth.kakao.com/oauth/authorize?..."
}
```

프론트 사용 방식:

1. 페이지 진입 시 이 API 호출
2. 응답받은 `authorizeUrl`로 브라우저 이동

### 2. 카카오 로그인 콜백

- Method: `GET`
- Path: `/api/auth/login/kakao/callback`
- Query:
  - `code: string`
- Auth: 불필요

동작:

- 서버가 카카오 인증을 처리한다
- 로그인 성공 시 인증 쿠키를 설정한다
- 이후 `frontendRedirectUri`로 `302 Redirect` 한다

프론트 참고:

- 웹에서는 이 엔드포인트를 브라우저 리다이렉트로 타게 될 가능성이 높다
- 별도 JSON 응답을 기대하지 않아야 한다

### 3. 카카오 로그인 직접 처리

- Method: `POST`
- Path: `/api/auth/login/kakao`
- Auth: 불필요

요청:

```json
{
  "code": "kakao-authorization-code"
}
```

응답:

```json
{
  "tokenType": "Bearer",
  "accessToken": "access-token",
  "refreshToken": "refresh-token",
  "refreshTokenExpiresAt": "2026-04-09T00:00:00Z",
  "role": "USER"
}
```

웹 참고:

- 응답 바디에도 토큰이 오지만 웹에서는 쿠키 기반으로 처리하면 된다
- 프론트에서 이 토큰 값을 저장하지 않는 쪽이 안전하다

### 4. 토큰 재발급

- Method: `POST`
- Path: `/api/auth/refresh`
- Auth: refresh token 쿠키 또는 요청 바디 필요

웹 요청 바디:

```json
{}
```

또는 body 없이 호출해도 된다. 서버가 쿠키에서 refresh token을 읽는다.

응답:

```json
{
  "tokenType": "Bearer",
  "accessToken": "new-access-token",
  "refreshToken": "new-refresh-token",
  "refreshTokenExpiresAt": "2026-04-10T00:00:00Z",
  "role": "USER"
}
```

프론트 권장 처리:

1. 보호 API에서 `401` 발생
2. `/api/auth/refresh` 호출
3. 성공 시 원 요청 재시도
4. 실패 시 로그아웃 처리 후 로그인 페이지 이동

### 5. 로그아웃

- Method: `POST`
- Path: `/api/auth/logout`
- Auth: refresh token 쿠키 또는 요청 바디 필요

웹 요청:

```json
{}
```

응답:

- Status: `204 No Content`
- 서버가 인증 쿠키를 제거한다

프론트 권장 처리:

1. 성공 여부와 무관하게 로컬 사용자 상태 초기화
2. 로그인 화면 또는 랜딩 화면 이동

### 6. [Dev] 테스트용 토큰 발급

**개발 환경(`dev` 프로필)에서만 사용 가능**

- Method: `POST`
- Path: `/api/dev/token`
- Query:
  - `userId: number` (필수)
  - `role?: string` (기본값: `USER`)
- Auth: 불필요

쿼리 예시:

```
POST /api/dev/token?userId=1&role=USER
```

응답:

```json
{
  "tokenType": "Bearer",
  "accessToken": "...",
  "refreshToken": "...",
  "refreshTokenExpiresAt": "2026-04-26T07:00:00Z",
  "role": "USER"
}
```

프론트 개발 시 사용 목적:

- 카카오 로그인 없이 바로 토큰 발급 받기
- 특정 역할(ADMIN 등)로 테스트하기
- 자동화 테스트 시 세션 설정

**주의**: 프로덕션에서는 이 엔드포인트가 비활성화되어 있음

## 사용자 API

### 1. 내 정보 조회

- Method: `GET`
- Path: `/api/users/me`
- Auth: 필요

응답:

```json
{
  "userId": 1,
  "id": "usr_123456",
  "nickname": "도치리",
  "profileImageUrl": "https://...",
  "role": "USER"
}
```

프론트 사용 위치:

- 앱 초기 로딩 시 세션 확인
- 헤더 프로필 영역
- 관리자 기능 노출 여부 판단

## 습관 API

습관(Habit)은 반복적으로 기록하는 행동 단위다. 각 습관에 기록(Record)을 남기면 잔디로 시각화된다.

### 1. 습관 목록 조회

- Method: `GET`
- Path: `/api/habits`
- Auth: 필요

응답:

```json
{
  "habits": [
    {
      "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
      "name": "물 마시기"
    },
    {
      "id": "c91caa47-92cc-4f56-bc51-c7d8165d8f98",
      "name": "러닝"
    }
  ]
}
```

### 2. 습관 생성

- Method: `POST`
- Path: `/api/habits`
- Auth: 필요

요청:

```json
{
  "name": "물 마시기"
}
```

응답:

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 마시기"
}
```

### 3. 습관 상세 조회

- Method: `GET`
- Path: `/api/habits/{habitId}`
- Auth: 필요

응답:

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 마시기"
}
```

### 4. 습관 이름 수정

- Method: `PATCH`
- Path: `/api/habits/{habitId}`
- Auth: 필요

요청:

```json
{
  "name": "물 10잔 마시기"
}
```

응답:

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 10잔 마시기"
}
```

### 5. 습관 삭제

- Method: `DELETE`
- Path: `/api/habits/{habitId}`
- Auth: 필요

응답:

- Status: `204 No Content`

### 6. 잔디 조회

- Method: `GET`
- Path: `/api/habits/grass`
- Auth: 필요

Query:

- `from?: YYYY-MM-DD` (기본값: 오늘 기준 18주 전)
- `to?: YYYY-MM-DD` (기본값: 오늘)

예시:

```text
GET /api/habits/grass?from=2026-01-01&to=2026-04-11
```

응답:

```json
{
  "fromDate": "2026-01-01",
  "toDate": "2026-04-11",
  "totalValue": 42,
  "days": [
    {
      "date": "2026-01-01",
      "value": 0,
      "level": 0
    },
    {
      "date": "2026-01-02",
      "value": 3,
      "level": 3
    }
  ]
}
```

`level` 규칙 (당일 모든 습관 기록의 `value` 합산 기준):

- `0`: 합산 0
- `1`: 합산 1
- `2`: 합산 2
- `3`: 합산 3~4
- `4`: 합산 5 이상

프론트 사용 위치:

- 월간/전체 잔디 캘린더
- 기간별 통계 영역

### 7. 습관 기록 목록 조회

- Method: `GET`
- Path: `/api/habits/{habitId}/records`
- Auth: 필요

Query:

- `from?: YYYY-MM-DD` (기본값: 오늘 기준 1개월 전)
- `to?: YYYY-MM-DD` (기본값: 오늘)

응답:

```json
{
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "records": [
    {
      "id": "a1b2c3d4-...",
      "completedAt": "2026-04-10T09:00:00Z",
      "value": 30
    }
  ]
}
```

- `value`: 기록된 값 (분 단위)

### 8. 습관 기록 생성

- Method: `POST`
- Path: `/api/habits/{habitId}/records`
- Auth: 필요

요청:

```json
{
  "completedAt": "2026-04-10T09:00:00Z",
  "value": 30
}
```

응답:

```json
{
  "id": "a1b2c3d4-...",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "value": 30
}
```

## 관리자 API

일반 사용자 웹에서는 우선순위가 낮지만, 관리자 화면이 필요하면 아래 API를 사용한다.

### 1. 사용자 권한 변경

- Method: `PATCH`
- Path: `/api/admin/users/{userId}/role`
- Auth: 필요 (`ADMIN`)

요청:

```json
{
  "role": "ADMIN"
}
```

응답:

- Status: `204 No Content`

## 프론트 구현 메모

### 인증 체크 흐름

1. 앱 시작
2. `GET /api/users/me`
3. 성공하면 로그인 상태
4. 실패하면 비로그인 상태

### API 클라이언트 공통 처리

- 모든 요청에 `credentials: 'include'`
- `401` 발생 시 `/api/auth/refresh` 1회 시도
- 재시도도 실패하면 로그인 페이지 이동

### 권장 React Query 키

- `['me']`
- `['habits']`
- `['habits', habitId]`
- `['habits', habitId, 'records', { from, to }]`
- `['grass', from, to]`

주의:

- 기록 생성 후에는 `['habits', habitId, 'records']`, `['grass']` 캐시를 함께 갱신하는 쪽이 가장 안전하다
- 습관 삭제 후에는 `['habits']`, `['grass']` 캐시를 갱신한다
