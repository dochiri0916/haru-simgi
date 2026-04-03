# kafka

## 역할

공통 Spring Kafka 기술 모듈이다.
Kafka 연동 시 반복되는 설정을 줄이고, 토픽 등록과 리스너 공통 설정의 기반을 제공한다.

## 제공 기능

- Spring Kafka 공통 의존성
- Kafka auto-configuration 보조 설정
- `dochiri.kafka.topics` 기반 토픽 자동 등록
- 공통 `CommonErrorHandler` 를 리스너 팩토리에 적용할 수 있는 확장 포인트

## 방향성

- 이 모듈은 Kafka 기술 기반만 제공한다.
- 이벤트 계약, 토픽 명명 규칙, 메시지 버전 전략은 서비스 또는 별도 아키텍처 규칙에서 관리한다.
- 장기적으로 retry, DLQ, 관측성, envelope 규약을 표준화할 수 있다.

## 넣지 않을 것

- 서비스별 이벤트 DTO
- 서비스별 producer / consumer 비즈니스 로직
- Kafka를 통한 도메인 정책 결정

## 사용 대상

- Kafka producer / consumer 를 사용하는 서비스
- 공통 토픽 등록과 리스너 설정이 필요한 서비스
