# PLAN

## 목표

- 사용자는 로그인 없이 앱을 바로 사용할 수 있어야 한다.
- 비회원 상태에서 생성한 TODO 데이터는 회원가입 또는 소셜 로그인 후 내 계정으로 이관되어야 한다.
- 로그인 전환은 사용 흐름을 끊지 않아야 하며, 이관은 가능한 한 자동으로 처리되어야 한다.

## 현재 상태

- 현재 저장소에는 `user-service`, `auth-service`, `gateway`가 있다.
- 회원가입과 로그인 토대는 있지만, 비회원 사용자 개념은 없다.
- TODO 도메인을 처리하는 별도 서비스 또는 API는 아직 없다.
- 소셜 로그인도 아직 구현되어 있지 않다.

즉, 이 요구사항을 만족하려면 인증 설계뿐 아니라 TODO 저장 주체와 데이터 이관 흐름까지 함께 추가해야 한다.

## 제품 시나리오

1. 사용자가 앱을 처음 실행한다.
2. 앱은 로컬 저장소에 `guestId`를 생성하고 저장한다.
3. 사용자는 로그인 없이 TODO를 생성, 수정, 삭제할 수 있다.
4. 이 시점의 데이터는 `guestId` 기준으로 서버에 저장하거나, 최소한 앱 로컬에 저장한다.
5. 사용자가 앱이 괜찮다고 느끼면 소셜 로그인 또는 일반 회원가입을 진행한다.
6. 로그인 성공 직후 앱은 `guestId`를 서버에 전달한다.
7. 서버는 `guestId`에 연결된 비회원 TODO 데이터를 로그인한 `userId`로 이관한다.
8. 이관이 끝나면 이후부터는 회원 계정 기준으로 계속 사용한다.

## 권장 아키텍처

### 1. 비회원 식별자 도입

- 앱 최초 실행 시 UUID 기반 `guestId`를 생성한다.
- `guestId`는 앱 로컬 저장소에 유지한다.
- 앱 삭제 전까지는 동일 기기에서 같은 비회원 사용자로 간주한다.
- 서버는 `guestId`를 신뢰 가능한 사용자 인증 수단으로 보지 않고, 비회원 데이터 식별용 키로만 사용한다.

### 2. TODO 저장 방식

가장 권장하는 방식은 서버 저장 방식이다.

- TODO 데이터는 서버에 저장한다.
- 레코드는 `ownerType`과 `ownerId`로 구분한다.
- 예시:
  - 비회원 TODO: `ownerType=GUEST`, `ownerId=<guestId>`
  - 회원 TODO: `ownerType=USER`, `ownerId=<userId>`

이 방식을 쓰면 앱 재설치 전까지는 안정적으로 이어서 사용할 수 있고, 회원 전환 시 서버에서 일괄 이관하기 쉽다.

클라이언트 로컬 저장 방식도 가능하지만 우선순위는 낮다.

- 비회원 TODO를 앱 로컬 DB 또는 스토리지에만 저장한다.
- 로그인 후 서버로 업로드한다.
- 단점은 기기 변경, 앱 삭제, 캐시 손실 시 데이터 복구가 안 된다는 점이다.

## 서비스 분리 제안

현재 저장소 기준 권장 확장 방향은 아래와 같다.

- `user-service`
  - 회원 프로필, 회원 식별, 가입 처리 담당
- `auth-service`
  - 일반 로그인, 소셜 로그인, 토큰 발급 담당
- `todo-service` 신규 추가 권장
  - TODO 생성, 조회, 수정, 삭제
  - 비회원 TODO와 회원 TODO 이관 처리

TODO 데이터는 인증과 별개로 수명주기가 길고, 이후 협업 기능이나 공유 기능이 붙을 가능성이 높기 때문에 `todo-service`로 분리하는 편이 낫다.

## 데이터 모델 초안

### GuestSession

- `guest_id`
- `device_id` 또는 `install_id` 선택 항목
- `created_at`
- `last_active_at`
- `migrated_user_id` nullable
- `migrated_at` nullable
- `status` (`ACTIVE`, `MIGRATED`, `EXPIRED`)

### Todo

- `id`
- `owner_type` (`GUEST`, `USER`)
- `owner_id`
- `title`
- `completed`
- `created_at`
- `updated_at`

### TodoMigrationHistory

- `id`
- `guest_id`
- `user_id`
- `migrated_count`
- `migration_key`
- `created_at`

이력 테이블은 중복 이관 방지와 운영 추적에 유용하다.

## API 초안

### 비회원 TODO API

- `POST /api/guest/todos`
- `GET /api/guest/todos?guestId=...`
- `PATCH /api/guest/todos/{todoId}`
- `DELETE /api/guest/todos/{todoId}`

실제 운영에서는 쿼리 파라미터보다 헤더 또는 바디 기반 전달이 더 낫다.

### 회원 TODO API

- `POST /api/todos`
- `GET /api/todos`
- `PATCH /api/todos/{todoId}`
- `DELETE /api/todos/{todoId}`

회원 API는 JWT 기반 사용자 식별을 사용한다.

### 데이터 이관 API

- `POST /api/todos/migrations/guest-to-user`

요청 예시:

```json
{
  "guestId": "4b7d7d3d-1fd6-4d7b-bf10-6dfe4fd5e9d3"
}
```

처리 규칙:

- 현재 로그인한 사용자 기준으로만 실행한다.
- 해당 `guestId`가 아직 이관되지 않았을 때만 성공한다.
- 성공 시 `guestId` 소유 TODO를 모두 현재 `userId`로 변경한다.
- 완료 후 `GuestSession.status=MIGRATED`로 변경한다.
- 재시도 시에는 멱등하게 동일 결과를 반환한다.

## 소셜 로그인 연동 흐름

### 앱 흐름

1. 앱 실행 시 `guestId` 확보
2. 비회원으로 TODO 사용
3. 사용자가 카카오, 구글, 애플 등으로 로그인
4. 로그인 성공 후 액세스 토큰 획득
5. 앱이 `guestId`와 함께 이관 API 호출
6. 서버가 이관 완료 응답 반환
7. 앱은 회원 TODO 목록을 다시 조회해 화면 갱신

### 서버 흐름

1. `auth-service`가 소셜 인증 성공 후 내부 사용자 식별값을 만든다.
2. 신규 사용자면 `user-service`에 회원 생성 또는 동기화 처리
3. JWT 발급
4. 앱이 JWT로 `todo-service` 이관 API 호출
5. `todo-service`가 `guestId -> userId` 이전 수행

## 구현 순서 제안

### 1단계

- `todo-service` 모듈 추가
- TODO 엔티티, 저장소, CRUD API 구현
- 회원 TODO만 먼저 저장 가능하게 구성

### 2단계

- `guestId` 기반 비회원 TODO 저장 기능 추가
- `GuestSession` 테이블 추가
- 앱에서 `guestId` 생성 및 유지

### 3단계

- `guest-to-user` 이관 API 추가
- 멱등 처리와 이관 이력 저장
- 이관 후 조회 결과가 회원 TODO로 보이도록 정리

### 4단계

- `auth-service`에 소셜 로그인 추가
- 소셜 계정과 내부 사용자 계정 매핑
- 로그인 직후 앱에서 이관 API 자동 호출

### 5단계

- 중복 병합 정책 정리
- 만료된 비회원 데이터 정리 배치 추가
- 운영 로그 및 감사 항목 추가

## 병합 정책

기본 정책은 단순 이전이 가장 안전하다.

- 비회원 TODO는 로그인한 사용자 계정으로 전부 소유권 이전
- 동일 제목 TODO가 이미 회원 계정에 있어도 자동 중복 제거는 하지 않음
- 중복 제거가 필요하면 후속 기능으로 별도 설계

초기 버전에서 제목 기반 중복 병합을 넣으면 의도치 않은 데이터 손실 가능성이 크다.

## 보안과 운영 고려사항

- `guestId`는 인증 수단이 아니므로 민감 데이터 저장에 사용하면 안 된다.
- 이관 API는 반드시 로그인 사용자만 호출 가능해야 한다.
- 동일 `guestId`에 대한 이관은 한 번만 성공해야 한다.
- `guestId` 위조를 막을 수는 없으므로, 초기 범위는 개인 TODO 수준의 저위험 데이터에 한정하는 것이 적절하다.
- 비회원 데이터는 일정 기간 후 삭제하는 정책이 필요하다.
- 앱 재설치 시 `guestId`가 바뀌면 기존 비회원 데이터는 기본적으로 복구되지 않는다.

## 테스트 항목

- 비회원 TODO 생성, 수정, 삭제
- 같은 `guestId`로 재실행 시 데이터 유지
- 로그인 후 이관 API 성공
- 이미 이관된 `guestId` 재호출 시 멱등 응답
- 이관 후 비회원 목록은 비어 있고 회원 목록에 데이터 존재
- 다른 사용자 토큰으로는 동일 `guestId` 이관 불가 또는 정책상 차단
- 소셜 로그인 신규 가입, 기존 가입자 로그인 모두 정상 동작

## 결정 사항

- 비회원 사용 후 회원 전환은 기술적으로 가능하다.
- 이 프로젝트에서는 `guestId` 기반 비회원 식별 + `todo-service`에서의 소유권 이전 방식이 가장 현실적이다.
- 초기 구현은 자동 병합보다 안전한 단순 이관을 우선한다.
