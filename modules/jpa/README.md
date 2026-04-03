# jpa

## 역할

공통 JPA 기반 모듈이다.
엔티티 매핑, 감사, Querydsl 등 persistence 계층 구현에 필요한 기술 기반을 제공한다.

## 제공 기능

- Spring Data JPA 의존성
- Querydsl JPA / APT 설정
- 공통 엔티티 기반 클래스와 연계되는 persistence 기반

## 방향성

- 이 모듈은 공통 기술 모듈이다.
- 서비스별 repository 계약은 각 서비스의 `application/port/out` 에 둔다.
- JPA adapter 구현은 각 서비스 내부 `adapter/out/persistence` 에 둔다.

## 넣지 않을 것

- 서비스별 repository 인터페이스
- 도메인 비즈니스 규칙
- 특정 서비스 전용 엔티티 정책

## 사용 대상

- JPA를 사용하는 모든 서비스
- Querydsl 기반 조회 로직이 필요한 서비스
