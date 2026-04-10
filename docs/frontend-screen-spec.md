# Frontend Screen Spec

웹 프론트엔드 1차 버전 기준 화면 정의다. 목표는 "카카오 로그인 -> 투두 생성/완료 -> 잔디 확인" 흐름을 가장 빠르게 구현하는 것이다. React SPA 기준으로 작성했다.

## 제품 목표

- 로그인한 사용자가 오늘의 할 일을 등록한다
- 완료한 작업이 잔디로 시각화된다
- 사용자는 자신의 성취를 날짜 단위로 확인한다

핵심은 기능이 많아 보이는 투두 앱이 아니라, "완료 기록이 잔디로 누적되는 경험"이다.

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
  - 왼쪽 또는 상단: 오늘의 할 일 작성/리스트
  - 오른쪽 또는 하단: 잔디 캘린더와 통계

모바일에서는 세로 스택으로 내려가면 된다.

## 화면 1. 랜딩 / 로그인 화면

- Route: `/` 또는 `/login`
- 목적: 서비스 소개와 로그인 진입

### 보여줄 요소

- 서비스 이름
- 한 줄 설명
  - 예: "완료한 할 일이 잔디로 쌓이는 투두 리스트"
- 카카오 로그인 버튼
- 비로그인 상태 설명
  - 로그인 후 내 투두와 잔디를 관리할 수 있음

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
- 목적: 투두 입력, 완료, 잔디 확인을 한 화면에서 처리

### 섹션 구성

#### 1. 헤더 영역

- 사용자 프로필 이미지
- 사용자 닉네임
- 로그아웃 버튼

사용 API:

- `GET /api/users/me`
- `POST /api/auth/logout`

#### 2. 할 일 입력 영역

- 입력창
- 추가 버튼

입력 규칙:

- 빈 문자열 금지
- 최대 100자
- `dueDate` 필수 (날짜 선택 UI 제공 권장)

사용 API:

- `POST /api/tasks`

성공 시 처리:

- 입력창 초기화
- 오늘 할 일 UI에 즉시 반영
- 잔디/통계 새로고침 트리거

#### 3. 오늘의 할 일 영역

이제 백엔드에서 목록 조회, 완료 취소, 삭제까지 지원하므로 일반적인 투두 UI를 구현할 수 있다.

표시 요소:

- 할 일 제목
- 완료 여부 배지
- 완료 버튼
- 완료 취소 버튼
- 삭제 버튼
- 완료 시각

사용 API:

- `GET /api/tasks`
- `PATCH /api/tasks/{taskId}/complete`
- `PATCH /api/tasks/{taskId}/reopen`
- `DELETE /api/tasks/{taskId}`

상태:

- `empty`: 아직 등록된 할 일이 없음
- `active`: 미완료 할 일 존재
- `submitting`: 완료 처리 중
- `error`: 완료 실패

#### 4. 잔디 캘린더 영역

서비스의 핵심 시각화 영역이다.

표시 요소:

- 월 이동 버튼
- 현재 조회 중인 연월
- 잔디 셀 그리드
- 범례
  - 연한 색: `level 0`
  - 진한 색: `level 4`
- 합계 통계
  - 기간 내 총 완료 수

사용 API:

- `GET /api/tasks/grass?from=YYYY-MM-DD&to=YYYY-MM-DD`

셀 데이터 규칙:

- `date`
- `completedCount`
- `level`

UI 규칙:

- hover 시 날짜와 완료 개수 툴팁 노출
- 오늘 날짜는 테두리 강조 가능
- 완료 0건도 빈칸이 아니라 셀로 유지

#### 5. 간단 통계 영역

초기 버전에서는 잔디 응답만으로도 아래 값은 계산 가능하다.

- 총 완료 수
- 완료한 날짜 수
- 가장 많이 완료한 날

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

- `selectedMonth`
- `from`
- `to`
- `days`
- `totalCompletedCount`

### 투두 입력 상태

- `title`
- `isSubmitting`
- `errorMessage`

## 컴포넌트 제안

권장 컴포넌트 분리:

- `AppLayout`
- `AuthGuard`
- `LoginPage`
- `DashboardPage`
- `UserMenu`
- `TaskComposer`
- `TaskList`
- `TaskItem`
- `GrassCalendar`
- `GrassLegend`
- `GrassTooltip`
- `StatsPanel`

## React Query 기준 데이터 흐름

### 필수 Query

- `['me']`
- `['grass', from, to]`
- `['tasks']`

### 필수 Mutation

- `login`
- `logout`
- `createTask`
- `completeTask`
- `reopenTask`
- `deleteTask`
- `refreshToken`

### invalidate 전략

- `createTask` 성공 후
  - `['tasks']` invalidate
  - `['grass', from, to]` invalidate
- `completeTask` 성공 후
  - `['tasks']` invalidate
  - `['grass', from, to]` invalidate
- `reopenTask` 성공 후
  - `['tasks']` invalidate
  - `['grass', from, to]` invalidate
- `deleteTask` 성공 후
  - `['tasks']` invalidate
  - `['grass', from, to]` invalidate
- `logout` 성공 후
  - `['me']` 초기화
  - 잔디 관련 캐시 제거

## MVP 범위

1차 구현은 아래까지만 해도 된다.

1. 로그인
2. 내 정보 조회
3. 할 일 목록 조회
4. 할 일 생성
5. 할 일 완료
6. 할 일 완료 취소
7. 할 일 삭제
8. 잔디 조회
9. 로그아웃

제외 가능 항목:

- 관리자 화면
- 복잡한 필터
- 다중 뷰
- 할 일 수정/삭제
- 다크 모드

## 백엔드 기준 제약 사항

현재 백엔드에는 아래 API가 아직 없다.

- 할 일 수정
- 미완료/완료 전체 목록 조회

따라서 프론트가 완전한 투두 리스트 UX를 제공하려면 다음 API가 추후 추가되어야 한다.

- `PATCH /api/tasks/{taskId}`

지금 시점의 웹 MVP는 "잔디 중심 대시보드"로 잡는 것이 가장 맞다.
