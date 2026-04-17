# Habit Service API Spec

`habit-service`는 습관 생성, 조회, 수정, 삭제와 습관 완료 기록 생성, 조회, 수정, 삭제, 잔디 조회를 담당한다. 외부 클라이언트는 Gateway를 통해 `/api/habits/**` 경로로 접근한다.

이 문서는 클라이언트 연동 기준 API 계약이다. 완료 기록의 날짜 경계, 하루 1회 완료, 잔디 집계 규칙은 [Habit Service Domain Definition](habit-service-domain.md)을 따른다.

## 기본 정보

- Gateway Base URL: `http://localhost:8080`
- Service Base URL: `http://localhost:8083`
- 공통 Prefix: `/api/habits`
- Content-Type: `application/json`
- 인증: 필요

인증 토큰은 아래 중 하나로 전달한다.

- 웹: `access_token` HttpOnly Cookie
- 앱/테스트 클라이언트: `Authorization: Bearer <accessToken>`

웹 클라이언트는 쿠키 전송을 위해 아래 설정을 사용한다.

- `fetch`: `credentials: 'include'`
- `axios`: `withCredentials: true`

Gateway는 보호 API 요청마다 JWT 서명/만료와 Redis 인증 세션 존재 여부를 함께 검증한다. 따라서 서버에서 로그아웃, 권한 변경, 세션 만료 등으로 Redis 세션이 삭제되면 아직 만료되지 않은 `accessToken`도 더 이상 사용할 수 없다.

클라이언트 인증 처리 기준:

- `/api/habits/**` 요청에는 `access_token` 쿠키 또는 `Authorization: Bearer <accessToken>`만 사용한다.
- `refresh_token`은 `/api/auth/refresh`, `/api/auth/logout`에만 사용한다. `refresh_token`을 Habit API의 Bearer 토큰으로 보내면 `401 UNAUTHORIZED`가 반환된다.
- Habit API에서 `401 UNAUTHORIZED`를 받으면 `/api/auth/refresh`로 토큰을 재발급한 뒤 원 요청을 한 번 재시도한다.
- refresh도 실패하면 클라이언트 인증 상태를 초기화하고 로그인 화면으로 이동한다.

## 공통 응답 규칙

- 날짜: `YYYY-MM-DD`
- 일시: ISO 8601 Instant 문자열, 예: `2026-04-10T09:00:00Z`
- `habitId`, `recordId`: UUID 문자열
- 성공 응답은 JSON 바디를 반환한다. 삭제 API는 `204 No Content`를 반환한다.

## 공통 에러 응답

서버는 Spring `ProblemDetail` 기반 JSON을 반환한다.

```json
{
  "type": "/errors/habit-not-found",
  "title": "HABIT_NOT_FOUND",
  "status": 404,
  "detail": "해당 습관을 찾을 수 없습니다.",
  "instance": "/api/habits/7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "code": "HABIT_NOT_FOUND"
}
```

주요 에러 코드는 아래와 같다.

| HTTP Status | Code | 설명 |
| --- | --- | --- |
| 400 | `INVALID_HABIT_ID` | 습관 ID 형식이 유효하지 않음 |
| 400 | `INVALID_HABIT_NAME` | 습관 이름이 비어 있거나 50자를 초과함 |
| 400 | `INVALID_HABIT_COLOR` | 지원하지 않는 습관 색상 |
| 400 | `INVALID_HABIT_DURATION` | `minutes`가 0 미만 또는 1440 초과 |
| 400 | `INVALID_HABIT_MEMO` | 메모가 200자를 초과함 |
| 400 | `INVALID_COMPLETED_AT` | 완료 시각이 유효하지 않음 |
| 401 | `UNAUTHORIZED` | 인증 필요 |
| 403 | `HABIT_ACCESS_DENIED` | 다른 사용자의 습관에 접근 |
| 404 | `HABIT_NOT_FOUND` | 습관을 찾을 수 없음 |
| 404 | `HABIT_RECORD_NOT_FOUND` | 습관 기록을 찾을 수 없음 |
| 409 | `DUPLICATE_HABIT_RECORD` | 기록 수정 시 같은 습관의 같은 기준일에 다른 기록이 이미 존재함 |
| 500 | `INTERNAL_SERVER_ERROR` | 서버 내부 오류 |

## 색상

습관 색상은 아래 값 중 하나다.

| color | colorHex |
| --- | --- |
| `BLUE` | `#3b82f6` |
| `GREEN` | `#10b981` |
| `RED` | `#ef4444` |
| `YELLOW` | `#f59e0b` |
| `PURPLE` | `#8b5cf6` |
| `PINK` | `#ec4899` |

`color`가 생략되거나 빈 문자열이면 기본값은 `GREEN`이다.

## 1. 습관 목록 조회

로그인한 사용자의 전체 습관 목록을 조회한다.

```http
GET /api/habits
```

### 응답

- Status: `200 OK`

```json
{
  "habits": [
    {
      "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
      "name": "물 마시기",
      "color": "BLUE",
      "colorHex": "#3b82f6",
      "index": 0,
      "createdAt": "2026-04-14T00:00:00Z"
    },
    {
      "id": "c91caa47-92cc-4f56-bc51-c7d8165d8f98",
      "name": "러닝",
      "color": "GREEN",
      "colorHex": "#10b981",
      "index": 1,
      "createdAt": "2026-04-14T00:05:00Z"
    }
  ]
}
```

## 2. 습관 생성

새 습관을 생성한다. 생성된 습관의 `index`는 사용자별 다음 순서로 자동 할당된다.

```http
POST /api/habits
```

### 요청

```json
{
  "name": "물 마시기",
  "color": "BLUE"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | string | Y | 습관 이름. 공백 불가, 최대 50자 |
| `color` | string | N | 색상. 생략 또는 빈 문자열이면 `GREEN` |

### 응답

- Status: `200 OK`

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 마시기",
  "color": "BLUE",
  "colorHex": "#3b82f6",
  "index": 0,
  "createdAt": "2026-04-14T00:00:00Z"
}
```

## 3. 습관 상세 조회

특정 습관의 상세 정보를 조회한다. 본인 소유 습관만 조회할 수 있다.

```http
GET /api/habits/{habitId}
```

### Path

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `habitId` | string | Y | 습관 ID |

### 응답

- Status: `200 OK`

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 마시기",
  "color": "BLUE",
  "colorHex": "#3b82f6",
  "index": 0,
  "createdAt": "2026-04-14T00:00:00Z"
}
```

## 4. 습관 이름 수정

특정 습관의 이름을 수정한다. 색상과 정렬 순서는 변경하지 않는다.

```http
PATCH /api/habits/{habitId}
```

### 요청

```json
{
  "name": "물 10잔 마시기"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `name` | string | Y | 변경할 습관 이름. 공백 불가, 최대 50자 |

### 응답

- Status: `200 OK`

```json
{
  "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "name": "물 10잔 마시기",
  "color": "BLUE",
  "colorHex": "#3b82f6",
  "index": 0,
  "createdAt": "2026-04-14T00:00:00Z"
}
```

## 5. 습관 정렬 순서 교환

두 습관의 정렬 순서를 서로 교환한다. 두 습관 모두 로그인한 사용자 소유여야 한다.

```http
PATCH /api/habits/index/swap
```

### 요청

```json
{
  "sourceHabitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "targetHabitId": "c91caa47-92cc-4f56-bc51-c7d8165d8f98"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `sourceHabitId` | string | Y | 정렬 순서를 바꿀 첫 번째 습관 ID |
| `targetHabitId` | string | Y | 정렬 순서를 바꿀 두 번째 습관 ID |

### 응답

- Status: `200 OK`

```json
{
  "habits": [
    {
      "id": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
      "name": "물 10잔 마시기",
      "color": "BLUE",
      "colorHex": "#3b82f6",
      "index": 1,
      "createdAt": "2026-04-14T00:00:00Z"
    },
    {
      "id": "c91caa47-92cc-4f56-bc51-c7d8165d8f98",
      "name": "러닝",
      "color": "GREEN",
      "colorHex": "#10b981",
      "index": 0,
      "createdAt": "2026-04-14T00:05:00Z"
    }
  ]
}
```

## 6. 습관 삭제

특정 습관을 삭제한다. 삭제 시 해당 습관의 기록도 함께 삭제된다.

```http
DELETE /api/habits/{habitId}
```

### 응답

- Status: `204 No Content`
- Body: 없음

## 7. 습관 기록 목록 조회

특정 습관의 완료 기록을 조회한다. 본인 소유 습관만 조회할 수 있다. 기간을 지정하지 않으면 전체 기록을 반환한다.

```http
GET /api/habits/{habitId}/records?from=2026-04-01&to=2026-04-30
```

### Query

| 이름 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `from` | date | N | 없음 | 조회 시작일, 양 끝 포함 |
| `to` | date | N | 없음 | 조회 종료일, 양 끝 포함 |

날짜 범위를 지정하면 서비스 기준 시간대의 시작/끝으로 변환해 조회한다. MVP 기준 시간대는 `Asia/Seoul`이다.

### 응답

- Status: `200 OK`

```json
{
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "records": [
    {
      "id": "a1b2c3d4-0000-0000-0000-000000000000",
      "completedAt": "2026-04-10T09:00:00Z",
      "minutes": 30,
      "memo": "클린 아키텍처"
    },
    {
      "id": "b2c3d4e5-0000-0000-0000-000000000000",
      "completedAt": "2026-04-11T09:00:00Z",
      "minutes": null,
      "memo": null
    }
  ]
}
```

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `records[].id` | string | 기록 ID |
| `records[].completedAt` | instant | 완료 시각 |
| `records[].minutes` | number \| null | 선택 입력인 소요 시간. `null`이면 시간을 입력하지 않은 완료 기록 |
| `records[].memo` | string \| null | 선택 입력인 메모. `null`이면 메모를 입력하지 않은 완료 기록 |

## 8. 습관 기록 생성

특정 습관의 완료 기록을 생성한다. 본인 소유 습관에만 기록할 수 있다.

완료 기록은 기록이 생성됐다는 사실 자체로 완료를 의미한다. `minutes`는 선택 입력이며, 완료 여부를 판단하는 값이 아니다.

이 API는 같은 습관의 같은 Business Date에 대해 멱등적이다. 이미 같은 날 완료 기록이 있으면 새 기록을 생성하지 않고 기존 기록을 `200 OK`로 반환한다.

```http
POST /api/habits/{habitId}/records
```

### 요청

```json
{
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30,
  "memo": "클린 아키텍처"
}
```

완료만 체크하는 요청은 아래처럼 보낼 수 있다.

```json
{}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `completedAt` | instant | N | 완료 시각. 생략 시 서버 현재 시각 사용 |
| `minutes` | number | N | 선택 입력인 소요 시간. 생략하면 `null`, 입력 시 `0 <= minutes <= 1440` |
| `memo` | string | N | 선택 입력인 메모. 생략하면 `null`, 최대 200자 |

요청 바디별 의미는 아래와 같다.

| 요청 | 의미 |
| --- | --- |
| `{}` | 지금 완료. 소요 시간, 메모 입력 없음 |
| `{ "minutes": 0 }` | 지금 완료. 0분을 명시적으로 입력 |
| `{ "minutes": 30 }` | 지금 완료. 30분 입력 |
| `{ "completedAt": "2026-04-10T09:00:00Z" }` | 지정 시각에 완료. 소요 시간 입력 없음 |
| `{ "memo": "클린 아키텍처" }` | 지금 완료. 메모 입력 |

### 응답

- Status: `200 OK`
- 새 기록이 생성된 경우와 기존 기록이 반환된 경우 모두 `200 OK`를 사용한다.

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30,
  "memo": "클린 아키텍처"
}
```

완료만 체크한 기록은 `minutes`, `memo`가 `null`로 반환된다. 이 기록도 잔디 집계에서는 완료 1건으로 계산된다.

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": null,
  "memo": null
}
```

### 중복 완료 규칙

- 하나의 습관은 하나의 Business Date에 최대 한 번만 완료할 수 있다.
- Business Date는 `completedAt`을 `Asia/Seoul`로 변환한 날짜다.
- 같은 Business Date에 다시 완료하면 새 기록을 생성하지 않고 기존 완료 기록을 반환한다.
- 중복 요청의 `minutes`, `memo`, `completedAt`은 기존 기록을 수정하지 않는다. 기존 기록을 변경하려면 습관 기록 수정 API를 사용한다.

예를 들어 이미 아래 기록이 존재한다고 가정한다.

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30,
  "memo": "클린 아키텍처"
}
```

같은 습관에 대해 `Asia/Seoul` 기준 같은 날짜인 아래 요청을 다시 보내면:

```json
{
  "completedAt": "2026-04-10T12:00:00Z",
  "minutes": 60,
  "memo": "새 메모"
}
```

응답은 새 요청 값이 아니라 기존 기록이다.

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30,
  "memo": "클린 아키텍처"
}
```

## 9. 습관 기록 수정

특정 습관의 완료 기록을 수정한다. 본인 소유 습관의 기록만 수정할 수 있다.

```http
PATCH /api/habits/{habitId}/records/{recordId}
```

### Path

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `habitId` | string | Y | 습관 ID |
| `recordId` | string | Y | 기록 ID |

### 요청

수정할 필드만 보내면 된다. 보내지 않은 필드는 기존 값을 유지한다.

```json
{
  "completedAt": "2026-04-10T10:00:00Z",
  "minutes": 45,
  "memo": "리팩터링"
}
```

| 필드 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `completedAt` | instant | N | 변경할 완료 시각. 생략하면 기존 값 유지 |
| `minutes` | number | N | 선택 입력인 소요 시간. 생략하거나 `null`이면 기존 값 유지, 입력 시 `0 <= minutes <= 1440` |
| `memo` | string \| null | N | 메모. 생략하면 기존 값 유지. `null`로 보내면 메모 삭제. 최대 200자 |

요청 바디별 의미는 아래와 같다.

| 요청 | 의미 |
| --- | --- |
| `{ "minutes": 45 }` | 완료 시각은 유지하고 소요 시간을 45분으로 수정 |
| `{ "minutes": null }` | 완료 시각과 소요 시간을 모두 기존 값으로 유지 |
| `{ "completedAt": "2026-04-10T10:00:00Z" }` | 완료 시각만 수정하고 소요 시간은 유지 |
| `{ "completedAt": "2026-04-10T10:00:00Z", "minutes": null }` | 완료 시각만 수정하고 소요 시간은 유지 |
| `{ "memo": "리팩터링" }` | 메모만 수정 |
| `{ "memo": null }` | 메모 삭제 |

### 응답

- Status: `200 OK`

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T10:00:00Z",
  "minutes": 45,
  "memo": "리팩터링"
}
```

소요 시간, 메모가 없는 기록은 각각 `null`로 반환된다.

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T10:00:00Z",
  "minutes": null,
  "memo": null
}
```

### 수정 규칙

- `recordId`가 `habitId`에 속하지 않으면 `404 HABIT_RECORD_NOT_FOUND`로 처리한다.
- `completedAt`을 수정하면 해당 완료 기록의 Business Date도 바뀔 수 있다.
- `minutes` 수정 여부와 관계없이 잔디 집계는 완료 기록 수 기준이다. `minutes: null`인 기록도 `value`에 1로 반영된다.
- 같은 습관의 같은 Business Date에 다른 기록이 이미 있다면 `409 DUPLICATE_HABIT_RECORD`로 처리한다.

## 10. 습관 기록 삭제

특정 습관의 완료 기록을 삭제한다. 본인 소유 습관의 기록만 삭제할 수 있다.

완료 취소는 해당 완료 기록을 삭제하는 방식으로 처리한다. 삭제된 기록은 기록 목록에서 제외되고, 잔디 집계에서도 해당 Business Date의 완료 수가 1 감소한다.

```http
DELETE /api/habits/{habitId}/records/{recordId}
```

### Path

| 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| `habitId` | string | Y | 습관 ID |
| `recordId` | string | Y | 기록 ID |

### 응답

- Status: `204 No Content`
- Body: 없음

### 삭제 규칙

- `habitId`가 다른 사용자의 습관이면 `403 HABIT_ACCESS_DENIED`로 처리한다.
- `recordId`가 `habitId`에 속하지 않으면 `404 HABIT_RECORD_NOT_FOUND`로 처리한다.
- 삭제 후 같은 Business Date에 다시 완료 기록을 생성할 수 있다.

## 11. 잔디 조회

로그인한 사용자의 전체 습관 완료 기록을 날짜별로 집계해 잔디 데이터로 반환한다.

```http
GET /api/habits/grass?from=2026-01-01&to=2026-04-11
```

### Query

| 이름 | 타입 | 필수 | 기본값 | 설명 |
| --- | --- | --- | --- | --- |
| `from` | date | N | 첫 습관 생성일 | 조회 시작일, 양 끝 포함 |
| `to` | date | N | 오늘 | 조회 종료일, 양 끝 포함 |

`from`을 생략하면 사용자의 첫 습관 생성일부터 잔디를 생성한다. 요청한 `from`이 첫 습관 생성일보다 앞서면 첫 습관 생성일로 보정한다. 습관이 하나도 없으면 `to` 날짜 하루를 기준으로 응답한다.

### 응답

- Status: `200 OK`

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

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| `fromDate` | date | 조회 시작일 |
| `toDate` | date | 조회 종료일 |
| `totalValue` | number | 조회 기간 내 완료 기록 수 합계 |
| `days[].date` | date | 날짜 |
| `days[].value` | number | 해당 날짜의 완료 기록 수 |
| `days[].level` | number | 잔디 레벨, `0~4` |

### 잔디 레벨

`level`은 해당 날짜의 완료 기록 수를 기준으로 계산한다. `minutes`는 잔디 `value`, `totalValue`, `level` 계산에 사용하지 않는다. 따라서 `minutes`가 `null`인 완료 기록도 해당 날짜 `value`에 1로 반영된다.

| 완료 기록 수 | level |
| --- | --- |
| 0 | 0 |
| 1 | 1 |
| 2 | 2 |
| 3~4 | 3 |
| 5 이상 | 4 |

## 클라이언트 캐시 갱신 기준

- 습관 생성 후: `GET /api/habits`, `GET /api/habits/grass` 캐시 갱신
- 습관 이름 수정 후: `GET /api/habits`, `GET /api/habits/{habitId}` 캐시 갱신
- 습관 정렬 순서 교환 후: `GET /api/habits`, `GET /api/habits/{sourceHabitId}`, `GET /api/habits/{targetHabitId}` 캐시 갱신
- 습관 삭제 후: `GET /api/habits`, `GET /api/habits/grass` 캐시 갱신
- 기록 생성 후: `GET /api/habits/{habitId}/records`, `GET /api/habits/grass` 캐시 갱신
- 기록 수정 후: `GET /api/habits/{habitId}/records`, `GET /api/habits/grass` 캐시 갱신
- 기록 삭제 후: `GET /api/habits/{habitId}/records`, `GET /api/habits/grass` 캐시 갱신

권장 React Query 키:

- `['habits']`
- `['habits', habitId]`
- `['habits', habitId, 'records', { from, to }]`
- `['grass', from, to]`

## OpenAPI

서비스 실행 후 아래 경로에서 OpenAPI JSON을 확인할 수 있다.

- Gateway: `GET /v3/api-docs/habit-service`
- Habit Service 직접 접근: `GET /v3/api-docs`
- Swagger UI: Gateway의 Swagger UI에서 `habit-service` 선택
