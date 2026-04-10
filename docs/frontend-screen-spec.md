# Frontend Screen Spec

웹 프론트엔드 1차 버전 기준 화면 정의다. 목표는 "카카오 로그인 -> 습관 생성/기록 -> 잔디 확인" 흐름을 가장 빠르게 구현하는 것이다. React SPA 기준으로 작성했다.

## 제품 목표

- 로그인한 사용자가 습관을 등록한다
- 완료한 습관 기록이 잔디로 시각화된다
- 사용자는 자신의 성취를 날짜 단위로 확인한다

핵심은 기능이 많아 보이는 앱이 아니라, "습관 기록이 잔디로 누적되는 경험"이다.

## 정보 구조

1. 비로그인 영역
2. 로그인 처리 영역
3. 메인 대시보드
4. 선택 기능: 관리자 화면

권장 라우트:

- `/`
- `/login`
- `/auth/callback`
- `/dashboard`
- `/admin` (선택)

## 공통 레이아웃

모든 로그인 후 화면은 아래 구조를 권장한다.

- 상단 헤더
  - 서비스 로고/이름
  - 현재 사용자 프로필
  - 로그아웃 버튼
- 메인 콘텐츠
  - 왼쪽 또는 상단: 습관 목록 및 기록 입력
  - 오른쪽 또는 하단: 잔디 캘린더와 통계

모바일에서는 세로 스택으로 내려가면 된다.

## 화면 1. 랜딩 / 로그인 화면

- Route: `/` 또는 `/login`
- 목적: 서비스 소개와 로그인 진입

### 보여줄 요소

- 서비스 이름
- 한 줄 설명
  - 예: "습관 기록이 잔디로 쌓이는 트래커"
- 카카오 로그인 버튼
- 비로그인 상태 설명
  - 로그인 후 내 습관과 잔디를 관리할 수 있음

### 주요 액션

- 카카오 로그인 버튼 클릭
  - `GET /api/auth/login/kakao/authorize`
  - 응답의 `authorizeUrl`로 이동

### 상태

- `idle`: 버튼 노출
- `loading`: 로그인 URL 조회 중
- `error`: 로그인 URL 조회 실패

### 에러 메시지 예시

- "로그인 URL을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요."

## 화면 2. 로그인 콜백 처리 화면

- Route: `/auth/callback`
- 목적: 로그인 직후 사용자 상태를 복구하고 메인 화면으로 이동

### 처리 방식

권장 방식은 둘 중 하나다.

1. 서버 콜백 URL을 직접 사용해 서버가 쿠키를 세팅하고 프론트 루트로 리다이렉트
2. 프론트 콜백 페이지에서 `code`를 읽고 `POST /api/auth/login/kakao` 호출

현재 백엔드 구조상 웹 우선이면 1번이 더 단순하다.

### 프론트 처리

- 진입 시 로딩 스피너 표시
- `GET /api/users/me` 호출
- 성공 시 `/dashboard` 이동
- 실패 시 `/login` 이동

### 상태

- `loading`: 로그인 확인 중
- `error`: 인증 실패

## 화면 3. 메인 대시보드

- Route: `/dashboard`
- 목적: 습관 관리, 기록 입력, 잔디 확인을 한 화면에서 처리

### 섹션 구성

#### 1. 헤더 영역

- 사용자 프로필 이미지
- 사용자 닉네임
- 로그아웃 버튼

사용 API:

- `GET /api/users/me`
- `POST /api/auth/logout`

#### 2. 습관 관리 영역

습관을 생성하고 목록을 관리하는 영역이다.

표시 요소:

- 습관 목록
- 습관 이름, 타입 배지
- 습관 추가 버튼
- 습관 이름 수정 버튼
- 습관 삭제 버튼

입력 규칙:

- `name`은 필수
- `type`은 `COUNT` 또는 `DURATION` 선택

사용 API:

- `GET /api/habits`
- `POST /api/habits`
- `PATCH /api/habits/{habitId}`
- `DELETE /api/habits/{habitId}`

성공 시 처리:

- 습관 목록 즉시 반영

#### 3. 습관 기록 입력 영역

선택한 습관에 오늘의 기록을 남기는 영역이다.

표시 요소:

- 기록할 습관 선택 (목록에서 선택)
- 완료 시각 (기본값: 현재 시각)
- 값 입력 (`value`: 횟수 또는 시간)
- 기록 추가 버튼

사용 API:

- `POST /api/habits/{habitId}/records`

성공 시 처리:

- 잔디/통계 새로고침 트리거

#### 4. 잔디 캘린더 영역

서비스의 핵심 시각화 영역이다.

표시 요소:

- 기간 이동 버튼 (기본: 최근 18주)
- 잔디 셀 그리드
- 범례
  - 연한 색: `level 0`
  - 진한 색: `level 4`
- 합계 통계
  - 기간 내 총 기록값 합산 (`totalValue`)

사용 API:

- `GET /api/habits/grass?from=YYYY-MM-DD&to=YYYY-MM-DD`

셀 데이터 규칙:

- `date`
- `value`: 해당일 모든 습관 기록 value 합산
- `level`: 합산 기준 0~4

UI 규칙:

- hover 시 날짜와 값 툴팁 노출
- 오늘 날짜는 테두리 강조 가능
- value 0인 날도 빈칸이 아니라 셀로 유지

#### 5. 간단 통계 영역

잔디 응답만으로 아래 값은 계산 가능하다.

- 기간 총 기록값 (`totalValue`)
- 기록이 있는 날짜 수 (`value > 0`인 날)
- 가장 높은 value를 기록한 날

별도 API 없이 프론트 계산으로 처리 가능하다.

## 화면 4. 세션 만료 처리

별도 페이지를 만들지 않아도 되지만 상태 설계는 필요하다.

### 동작 규칙

1. 보호 API가 `401` 반환
2. `POST /api/auth/refresh` 시도
3. 성공하면 원 요청 재시도
4. 실패하면 로그인 화면으로 이동

### 사용자 메시지

- 재발급 실패 시:
  - "세션이 만료되었습니다. 다시 로그인해 주세요."

## 화면 5. 관리자 화면 (선택)

- Route: `/admin`
- 노출 조건: `me.role === 'ADMIN'`

초기 버전에서는 필수는 아니다. 필요 시 아래 정도만 두면 된다.

- 사용자 ID 입력
- 권한 선택(`USER`, `ADMIN`)
- 권한 변경 버튼

사용 API:

- `PATCH /api/admin/users/{userId}/role`

## 상태 모델

프론트 전역에서 최소한 아래 상태는 있어야 한다.

### 인증 상태

- `unknown`
- `authenticated`
- `unauthenticated`

### 사용자 상태

- `me`
- `isAdmin`

### 잔디 상태

- `from`
- `to`
- `days`
- `totalValue`

### 습관 입력 상태

- `selectedHabitId`
- `value`
- `completedAt`
- `isSubmitting`
- `errorMessage`

## 컴포넌트 제안

권장 컴포넌트 분리:

- `AppLayout`
- `AuthGuard`
- `LoginPage`
- `DashboardPage`
- `UserMenu`
- `HabitList`
- `HabitItem`
- `HabitForm`
- `HabitRecordForm`
- `GrassCalendar`
- `GrassLegend`
- `GrassTooltip`
- `StatsPanel`

## React Query 기준 데이터 흐름

### 필수 Query

- `['me']`
- `['habits']`
- `['habits', habitId]`
- `['habits', habitId, 'records', { from, to }]`
- `['grass', from, to]`

### 필수 Mutation

- `login`
- `logout`
- `createHabit`
- `updateHabitName`
- `deleteHabit`
- `createHabitRecord`
- `refreshToken`

### invalidate 전략

- `createHabit` 성공 후
  - `['habits']` invalidate
- `updateHabitName` 성공 후
  - `['habits']` invalidate
  - `['habits', habitId]` invalidate
- `deleteHabit` 성공 후
  - `['habits']` invalidate
  - `['grass', from, to]` invalidate
- `createHabitRecord` 성공 후
  - `['habits', habitId, 'records']` invalidate
  - `['grass', from, to]` invalidate
- `logout` 성공 후
  - `['me']` 초기화
  - 잔디 관련 캐시 제거

## MVP 범위

1차 구현은 아래까지만 해도 된다.

1. 로그인
2. 내 정보 조회
3. 습관 목록 조회
4. 습관 생성
5. 습관 이름 수정
6. 습관 삭제
7. 습관 기록 생성
8. 잔디 조회
9. 로그아웃

제외 가능 항목:

- 관리자 화면
- 습관 기록 목록 조회 화면
- 다크 모드
