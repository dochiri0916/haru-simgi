# Habit Service Domain Definition

이 문서는 `habit-service`의 도메인 기준 명세다. README, 프론트엔드 명세, API DTO, 저장소 제약, 테스트는 이 문서를 기준으로 맞춘다.

## 목적

`habit-service`는 사용자가 반복적으로 실천할 습관을 만들고, 습관 완료 기록을 날짜별로 집계해 잔디로 보여주는 서비스다.

핵심 흐름은 아래와 같다.

1. 사용자는 습관을 만든다.
2. 사용자는 하루에 한 번 습관 완료 버튼을 누른다.
3. 사용자는 원하면 소요 시간(분)을 함께 입력한다.
4. 서비스는 완료 기록을 날짜별로 집계한다.
5. 클라이언트는 집계된 값을 잔디 강도(`level 0~4`)로 표시한다.

## 용어

| 용어 | 의미 |
| --- | --- |
| Habit | 사용자가 반복적으로 수행하려는 습관이다. 소유자, 이름, 색상을 가진다. |
| HabitRecord | 특정 습관을 완료했다는 기록이다. 완료 시각과 선택 입력인 소요 시간을 가진다. |
| Completion | 사용자가 완료 버튼을 눌렀다는 사실이다. 소요 시간이 없어도 완료로 인정된다. |
| Minutes | 사용자가 선택적으로 입력하는 소요 시간이다. 완료 여부를 판단하는 값이 아니다. |
| Business Date | 완료 기록을 날짜 단위로 해석할 때 쓰는 서비스 기준일이다. MVP에서는 `Asia/Seoul` 기준이다. |
| Grass | 날짜별 완료 기록 수를 `level 0~4` 강도로 변환한 시각화 데이터다. |

## 도메인 규칙

### 1. 습관 소유권

- 모든 습관은 하나의 사용자 소유다.
- 습관 조회, 수정, 삭제, 기록 생성, 기록 조회는 습관 소유자만 수행할 수 있다.
- 요청 사용자가 습관 소유자가 아니면 `403 Forbidden`으로 처리한다.

### 2. 완료 기록

- 완료 버튼을 누르면 `HabitRecord`가 생성된다.
- 완료 기록은 `completed = true`인 사실 자체로 완료를 의미한다.
- `minutes`는 nullable이다.
- `minutes = null`은 "소요 시간을 입력하지 않음"이다.
- `minutes = 0`은 "0분을 명시적으로 입력함"이다.
- `minutes = null`과 `minutes = 0`은 모두 완료 기록으로 집계된다.
- `minutes`가 존재하면 `0 <= minutes <= 1440`이어야 한다.
- `completedAt`은 완료 시각이다. 외부 요청에서 생략되면 서버 현재 시각을 사용하고, 제공되면 제공된 시각을 사용한다.

### 3. 하루 1회 완료

- 하나의 습관은 하나의 Business Date에 최대 한 번만 완료할 수 있다.
- 중복 기준은 `(habitId, completionDate)`다.
- `completionDate`는 `completedAt`을 `Asia/Seoul`로 변환한 `LocalDate`다.
- 같은 습관을 같은 Business Date에 다시 완료하려 하면 `409 Conflict`로 처리한다.
- 같은 시각의 중복뿐 아니라 같은 날짜의 다른 시각 중복도 막아야 한다.

### 4. 날짜 경계

- MVP의 서비스 기준 시간대는 `Asia/Seoul`이다.
- 기록 조회와 잔디 조회는 같은 시간대 정책을 사용해야 한다.
- `from`, `to`는 `LocalDate`이며 양 끝을 포함한다.
- 내부 조회 구간은 `[from.atStartOfDay(zone), to.plusDays(1).atStartOfDay(zone))`로 변환한다.
- 예를 들어 `2026-04-09T15:30:00Z`는 한국 시간 `2026-04-10 00:30`이므로 Business Date는 `2026-04-10`이다. 이 기록은 `from=2026-04-10&to=2026-04-10`인 기록 조회와 잔디 조회에 모두 포함되어야 한다.

### 5. 잔디 집계

- 잔디 조회에서 `from`을 생략하면 사용자의 첫 습관 생성일부터 생성한다.
- 요청한 `from`이 첫 습관 생성일보다 앞서면 첫 습관 생성일로 보정한다.
- 습관이 하나도 없으면 `to` 날짜 하루를 기준으로 응답한다.
- 잔디의 `value`는 해당 날짜의 완료 기록 수다.
- 잔디의 `totalValue`는 조회 기간 내 완료 기록 수의 합이다.
- 완료 기록 수는 사용자가 소유한 모든 습관의 완료 기록을 합산한다.
- 소요 시간이 없는 완료 기록도 `value`에 1로 반영된다.
- `minutes`는 잔디 강도 계산에 사용하지 않는다.
- 소요 시간 기반 통계가 필요하면 `totalMinutes`, `minutesByDate`처럼 별도의 명시적인 필드나 API를 추가한다.

잔디 레벨 기준은 아래와 같다.

| 완료 기록 수 | level |
| --- | --- |
| 0 | 0 |
| 1 | 1 |
| 2 | 2 |
| 3~4 | 3 |
| 5 이상 | 4 |

### 6. 습관 기록 조회

- 기록 조회는 특정 습관 하나의 완료 기록 목록을 반환한다.
- 조회 구간은 잔디 조회와 같은 Business Date 정책을 사용한다.
- 응답의 `minutes`는 nullable이다.
- 정렬 기준은 `completedAt` 오름차순을 기본값으로 한다.

## API 계약

### 완료 기록 생성

```http
POST /api/habits/{habitId}/records
```

요청 바디:

```json
{
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30
}
```

`completedAt`과 `minutes`는 모두 선택 값이다. 완료만 체크하는 요청은 아래처럼 보낼 수 있다.

```json
{}
```

응답 바디:

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": 30
}
```

완료만 체크한 기록의 응답 예시:

```json
{
  "id": "a1b2c3d4-0000-0000-0000-000000000000",
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "completedAt": "2026-04-10T09:00:00Z",
  "minutes": null
}
```

### 습관 기록 조회

```http
GET /api/habits/{habitId}/records?from=2026-04-01&to=2026-04-30
```

응답 바디:

```json
{
  "habitId": "7a2e41fd-8f5c-4d8b-9324-f39f4f76c5a8",
  "records": [
    {
      "id": "a1b2c3d4-0000-0000-0000-000000000000",
      "completedAt": "2026-04-10T09:00:00Z",
      "minutes": null
    }
  ]
}
```

### 잔디 조회

```http
GET /api/habits/grass?from=2026-04-01&to=2026-04-30
```

응답 바디:

```json
{
  "fromDate": "2026-04-01",
  "toDate": "2026-04-30",
  "totalValue": 3,
  "days": [
    {
      "date": "2026-04-01",
      "value": 0,
      "level": 0
    },
    {
      "date": "2026-04-02",
      "value": 1,
      "level": 1
    },
    {
      "date": "2026-04-03",
      "value": 2,
      "level": 2
    }
  ]
}
```

## 저장 모델 기준

- `HabitRecord`는 `completedAt: Instant`, `completionDate: LocalDate`, `durationMinutes: Integer`를 저장해야 한다.
- `completionDate`는 `completedAt`을 서비스 기준 시간대(`Asia/Seoul`)로 변환해 계산한다.
- DB는 `(habit_id, completion_date)` 유니크 제약을 가져야 한다.
- 저장 전 서비스 레벨에서도 같은 `(habitId, completionDate)` 기록 존재 여부를 확인해 도메인 예외를 던진다.
- DB 유니크 제약 위반도 최종적으로 `409 Conflict` 응답으로 매핑되어야 한다.

## 에러 기준

| 상황 | 응답 |
| --- | --- |
| 습관을 찾을 수 없음 | `404 Not Found` |
| 습관 소유자가 아님 | `403 Forbidden` |
| 잘못된 `minutes` | `400 Bad Request` |
| 같은 습관을 같은 Business Date에 중복 완료 | `409 Conflict` |
| `from`이 `to`보다 늦음 | `400 Bad Request` |

## 수용 기준

- `minutes` 없이 완료 기록을 생성하면 잔디의 해당 날짜 `value`가 1 증가하고 `level`은 최소 1이 된다.
- `minutes = 0`으로 완료 기록을 생성해도 잔디의 해당 날짜 `value`가 1 증가한다.
- 같은 습관을 같은 Business Date에 두 번 완료하면 두 번째 요청은 `409 Conflict`가 된다.
- 한국 시간 `2026-04-10 00:30`에 해당하는 기록은 기록 조회와 잔디 조회 모두 `2026-04-10` 데이터로 나온다.
- 기록 조회와 잔디 조회는 같은 날짜 범위 입력에 대해 같은 Business Date 경계를 사용한다.
- 잔디 `value`와 `totalValue`는 분 합계가 아니라 완료 기록 수다.

## 현재 구현과의 차이

이 섹션은 구현 작업의 체크리스트다.

- `GetHabitGrassService`는 duration 합계가 아니라 완료 기록 수를 날짜별로 집계해야 한다.
- `GrassLevelPolicy`는 분 기준이 아니라 완료 기록 수 기준으로 동작해야 한다.
- `GetHabitRecordsCommand`와 `GetHabitGrassService`는 같은 `Asia/Seoul` 날짜 경계 정책을 사용해야 한다.
- `HabitRecordEntity`의 유니크 제약은 `(habit_id, completed_at)`이 아니라 `(habit_id, completion_date)`가 되어야 한다.
- `CreateHabitRecordService`는 저장 전 같은 Business Date 기록 존재 여부를 확인해야 한다.
- 중복 완료 도메인 예외와 `409 Conflict` 매핑을 추가해야 한다.
- `habit-service` 테스트에 완료만 체크, 0분 입력, 중복 완료, 날짜 경계, 잔디 집계 기준을 검증하는 케이스를 추가해야 한다.
