# 주식 모의투자 웹 애플리케이션 요구사항 정의서

작성일: 2026-06-08

## 1. 프로젝트 개요

### 1.1 프로젝트명

Spring 기반 주식 모의투자 웹 애플리케이션

### 1.2 프로젝트 목적

사용자가 가상 현금을 지급받고, 미리 등록된 주식 종목을 매수/매도하며, 보유 종목과 거래 내역, 평가손익, 수익률을 확인할 수 있는 모의투자 서비스를 구현한다.

이 프로젝트의 핵심은 실제 증권 서비스 수준의 복잡한 주문 시스템이 아니라, Spring과 JPA를 사용해 다음 흐름을 정확하게 구현하는 것이다.

```text
회원가입
-> 가상 계좌 생성
-> 종목 조회
-> 매수/매도
-> 현금 잔고 변경
-> 보유 종목 변경
-> 거래 내역 저장
-> 포트폴리오 조회
```

### 1.3 핵심 학습 목표

- Spring Boot 기반 웹 애플리케이션 구조 이해
- Spring Data JPA를 이용한 도메인 설계
- 매수/매도 비즈니스 로직 구현
- `@Transactional`을 이용한 데이터 정합성 보장
- 계좌 잔고와 보유 수량에 대한 검증 로직 구현
- Spring Security 세션 로그인을 이용한 사용자 인증
- 동시 요청 상황에서 계좌 데이터가 잘못 변경되지 않도록 제어

### 1.4 개발 범위

2~3주 안에 완성 가능한 MVP를 기준으로 한다.

필수 범위:

- 회원가입 / 로그인 / 로그아웃
- 가상 계좌 생성
- 종목 목록 / 상세 / 검색
- 매수
- 매도
- 보유 종목 조회
- 계좌 요약 조회
- 거래 내역 조회
- 수익률 계산
- 트랜잭션 적용
- 주요 비즈니스 로직 테스트

선택 범위:

- 종목 현재가 랜덤 변동
- 거래 내역 조건 검색 고도화
- 수익률 랭킹
- 관심 종목
- QueryDSL
- Redis 캐싱
- Docker 배포

제외 범위:

- 실제 주식 API 연동
- 실시간 호가창
- 차트
- 지정가/시장가 주문 구분
- 주문 대기/체결 시스템
- OAuth 로그인
- JWT 기반 인증
- 관리자 기능

## 2. 사용자 및 권한

### 2.1 사용자 유형

| 사용자 | 설명 |
| --- | --- |
| 비회원 | 회원가입, 로그인, 종목 조회만 가능 |
| 회원 | 계좌 조회, 매수, 매도, 보유 종목 조회, 거래 내역 조회 가능 |

### 2.2 인증 방식

- Spring Security 세션 로그인을 사용한다.
- 로그인 성공 시 서버 세션에 인증 상태를 저장한다.
- JWT, Refresh Token, OAuth는 MVP 범위에서 제외한다.
- 비밀번호는 평문으로 저장하지 않고 `PasswordEncoder`로 암호화한다.

### 2.3 인가 규칙

- 로그인하지 않은 사용자는 계좌, 매수, 매도, 보유 종목, 거래 내역 API에 접근할 수 없다.
- 사용자는 본인의 계좌만 조회할 수 있다.
- 사용자는 본인의 보유 종목만 조회할 수 있다.
- 사용자는 본인의 거래 내역만 조회할 수 있다.
- 다른 사용자의 계좌, 보유 종목, 거래 내역을 조회하거나 변경할 수 없다.

## 3. 기능 요구사항

### 3.1 회원 기능

#### FR-USER-001 회원가입

- 사용자는 이메일, 비밀번호, 닉네임을 입력해 회원가입할 수 있다.
- 이메일은 중복될 수 없다.
- 비밀번호는 암호화하여 저장한다.
- 회원가입 성공 시 사용자별 가상 계좌를 자동 생성한다.
- 생성된 계좌에는 초기 가상 현금 `10,000,000원`을 지급한다.

#### FR-USER-002 로그인

- 사용자는 이메일과 비밀번호로 로그인할 수 있다.
- 로그인 성공 시 세션이 생성된다.
- 로그인 실패 시 실패 사유를 응답한다.

#### FR-USER-003 로그아웃

- 로그인한 사용자는 로그아웃할 수 있다.
- 로그아웃 시 세션을 만료한다.

#### FR-USER-004 내 정보 조회

- 로그인한 사용자는 본인의 회원 정보를 조회할 수 있다.
- 응답에는 비밀번호를 포함하지 않는다.

### 3.2 계좌 기능

#### FR-ACCOUNT-001 계좌 자동 생성

- 회원가입이 완료되면 해당 사용자에게 계좌가 자동 생성된다.
- 사용자와 계좌는 1:1 관계를 가진다.

#### FR-ACCOUNT-002 내 계좌 조회

- 로그인한 사용자는 본인의 계좌 요약 정보를 조회할 수 있다.
- 계좌 요약에는 현금 잔고, 주식 평가금액, 총 자산, 평가손익, 수익률을 포함한다.

계산식:

```text
주식 평가금액 = 보유수량 * 현재가
총 자산 = 현금 잔고 + 주식 평가금액
평가손익 = 주식 평가금액 - 총 매입금액
수익률 = 평가손익 / 총 매입금액 * 100
```

단, 총 매입금액이 0인 경우 수익률은 0으로 처리한다.

### 3.3 종목 기능

#### FR-STOCK-001 종목 초기 데이터 등록

- 종목 데이터는 실제 주식 API를 연동하지 않고 DB에 미리 등록한다.
- 미래 가격 데이터를 넣는 것이 아니라, 거래 기준이 되는 현재가를 가진 종목 기본 정보를 등록한다.

초기 종목 예시:

| 종목코드 | 종목명 | 시장 | 현재가 |
| --- | --- | --- | ---: |
| 005930 | 삼성전자 | KOSPI | 78,000 |
| 000660 | SK하이닉스 | KOSPI | 180,000 |
| 035420 | NAVER | KOSPI | 180,000 |
| 035720 | 카카오 | KOSPI | 45,000 |
| 005380 | 현대차 | KOSPI | 250,000 |
| 373220 | LG에너지솔루션 | KOSPI | 350,000 |

#### FR-STOCK-002 종목 목록 조회

- 사용자는 등록된 종목 목록을 조회할 수 있다.
- 종목 목록은 종목명, 종목코드, 시장 구분, 현재가를 포함한다.
- 목록 조회는 추후 페이징을 적용할 수 있도록 설계한다.

#### FR-STOCK-003 종목 상세 조회

- 사용자는 특정 종목의 상세 정보를 조회할 수 있다.
- 존재하지 않는 종목 ID로 조회하면 실패 응답을 반환한다.

#### FR-STOCK-004 종목 검색

- 사용자는 종목명 또는 종목코드로 종목을 검색할 수 있다.
- 검색어가 없으면 전체 목록 조회와 동일하게 처리하거나, 잘못된 요청으로 처리한다.

### 3.4 매수 기능

#### FR-TRADE-001 매수 요청

- 로그인한 사용자는 종목 ID와 수량을 입력해 매수할 수 있다.
- 매수 수량은 1 이상이어야 한다.
- 매수 가격은 요청 시점의 종목 현재가를 기준으로 한다.
- 주문 금액은 `현재가 * 수량`으로 계산한다.

#### FR-TRADE-002 매수 검증

- 존재하지 않는 종목은 매수할 수 없다.
- 현금 잔고가 주문 금액보다 부족하면 매수는 실패한다.
- 실패한 매수는 현금 잔고, 보유 종목, 거래 내역에 아무 변경도 남기지 않는다.

#### FR-TRADE-003 매수 성공 처리

매수 성공 시 다음 처리를 하나의 트랜잭션으로 수행한다.

1. 계좌 현금 잔고 차감
2. 보유 종목 생성 또는 수량 증가
3. 평균 매입가 재계산
4. 거래 내역 저장

평균 매입가 계산식:

```text
새 평균 매입가 = (기존 총 매입금액 + 추가 매입금액) / 총 보유수량
```

### 3.5 매도 기능

#### FR-TRADE-004 매도 요청

- 로그인한 사용자는 본인이 보유한 종목을 매도할 수 있다.
- 매도 요청에는 종목 ID와 수량이 포함된다.
- 매도 수량은 1 이상이어야 한다.
- 매도 가격은 요청 시점의 종목 현재가를 기준으로 한다.

#### FR-TRADE-005 매도 검증

- 존재하지 않는 종목은 매도할 수 없다.
- 보유하지 않은 종목은 매도할 수 없다.
- 보유 수량보다 많은 수량은 매도할 수 없다.
- 실패한 매도는 현금 잔고, 보유 종목, 거래 내역에 아무 변경도 남기지 않는다.

#### FR-TRADE-006 매도 성공 처리

매도 성공 시 다음 처리를 하나의 트랜잭션으로 수행한다.

1. 보유 수량 감소
2. 계좌 현금 잔고 증가
3. 거래 내역 저장
4. 보유 수량이 0이면 보유 종목 삭제

### 3.6 보유 종목 기능

#### FR-HOLDING-001 내 보유 종목 조회

- 로그인한 사용자는 본인의 보유 종목 목록을 조회할 수 있다.
- 보유 종목이 없으면 빈 배열을 반환한다.

응답 항목:

- 종목 ID
- 종목명
- 종목코드
- 보유 수량
- 평균 매입가
- 현재가
- 평가금액
- 평가손익
- 수익률

계산식:

```text
평가금액 = 현재가 * 보유수량
평가손익 = 평가금액 - 총 매입금액
수익률 = 평가손익 / 총 매입금액 * 100
```

### 3.7 거래 내역 기능

#### FR-HISTORY-001 내 거래 내역 조회

- 로그인한 사용자는 본인의 전체 거래 내역을 조회할 수 있다.
- 거래 내역은 최신순으로 정렬한다.

응답 항목:

- 거래 ID
- 종목 ID
- 종목명
- 매수/매도 구분
- 거래 수량
- 거래 가격
- 거래 금액
- 거래 시각

#### FR-HISTORY-002 거래 내역 조건 조회

- 사용자는 거래 내역을 조건별로 조회할 수 있다.
- 조건은 선택적으로 전달할 수 있다.

검색 조건:

- 매수/매도 구분
- 종목 ID
- 시작일
- 종료일

MVP에서는 단순 조건 검색으로 구현하고, 복잡한 동적 검색은 선택 기능으로 분리한다.

## 4. 비기능 요구사항

### 4.1 트랜잭션 정합성

- 매수와 매도는 반드시 하나의 트랜잭션으로 처리한다.
- 중간 단계에서 예외가 발생하면 전체 작업을 롤백한다.
- 현금 잔고 변경, 보유 종목 변경, 거래 내역 저장 중 일부만 반영되는 상태를 허용하지 않는다.

적용 위치:

```java
@Transactional
public TradeResponse buyStock(Long userId, BuyRequest request) {
    // 계좌 조회
    // 종목 조회
    // 잔고 검증
    // 잔고 차감
    // 보유 종목 변경
    // 거래 내역 저장
}
```

### 4.2 동시성 제어

- 같은 사용자가 동시에 여러 매수 요청을 보내도 잔고가 음수가 되면 안 된다.
- 같은 사용자가 동시에 여러 매도 요청을 보내도 보유 수량이 음수가 되면 안 된다.
- 계좌 데이터를 변경하는 매수/매도 요청은 계좌 단위로 순차 처리되도록 한다.

권장 구현:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.user.id = :userId")
Optional<Account> findByUserIdForUpdate(Long userId);
```

매수/매도 서비스에서는 일반 계좌 조회가 아니라 잠금이 적용된 조회 메서드를 사용한다.

### 4.3 보안

- 비밀번호는 `BCryptPasswordEncoder` 등 안전한 해시 알고리즘으로 암호화한다.
- 인증이 필요한 API는 Spring Security 설정으로 보호한다.
- 응답 DTO에는 비밀번호를 포함하지 않는다.
- 서버 로그에 비밀번호, 세션 ID 같은 민감 정보를 출력하지 않는다.
- 사용자의 식별자는 요청 파라미터가 아니라 인증 정보에서 가져온다.

### 4.4 데이터 검증

- 이메일은 필수 값이며 이메일 형식을 만족해야 한다.
- 비밀번호는 필수 값이며 최소 길이를 만족해야 한다.
- 닉네임은 필수 값이다.
- 매수/매도 수량은 1 이상이어야 한다.
- 존재하지 않는 종목으로 거래할 수 없다.
- 잔고보다 큰 금액은 매수할 수 없다.
- 보유 수량보다 많은 수량은 매도할 수 없다.

### 4.5 예외 처리

- 공통 예외 응답 형식을 사용한다.
- 클라이언트가 이해할 수 있도록 에러 코드와 메시지를 반환한다.

예시:

```json
{
  "code": "INSUFFICIENT_CASH",
  "message": "현금 잔고가 부족합니다."
}
```

주요 에러 코드:

| 코드 | 설명 |
| --- | --- |
| `EMAIL_ALREADY_EXISTS` | 이미 가입된 이메일 |
| `INVALID_LOGIN` | 이메일 또는 비밀번호 불일치 |
| `UNAUTHORIZED` | 인증되지 않은 사용자 |
| `STOCK_NOT_FOUND` | 존재하지 않는 종목 |
| `ACCOUNT_NOT_FOUND` | 존재하지 않는 계좌 |
| `HOLDING_NOT_FOUND` | 보유하지 않은 종목 |
| `INSUFFICIENT_CASH` | 현금 잔고 부족 |
| `INSUFFICIENT_QUANTITY` | 보유 수량 부족 |
| `INVALID_QUANTITY` | 잘못된 거래 수량 |

### 4.6 테스트

필수 테스트:

- 회원가입 성공 시 계좌 자동 생성
- 매수 성공
- 잔고 부족으로 매수 실패
- 기존 보유 종목 추가 매수 시 평균 매입가 계산
- 매도 성공
- 보유 수량 부족으로 매도 실패
- 매도 후 보유 수량이 0이면 Holding 삭제
- 포트폴리오 수익률 계산
- 거래 내역 저장
- 동시 매수 요청 시 잔고 초과 방지
- 동시 매도 요청 시 보유 수량 초과 방지

우선순위:

1. Service 단위 테스트
2. Repository 락 테스트
3. Controller API 테스트

### 4.7 성능 및 확장성

- MVP에서는 높은 성능보다 정확한 비즈니스 로직을 우선한다.
- 종목 목록은 페이징 확장을 고려한다.
- 거래 내역은 최신순 정렬을 기본으로 한다.
- 거래 내역 조건 검색은 QueryDSL로 확장 가능하게 설계한다.
- Redis 캐싱은 필수가 아니라 선택 기능으로 둔다.

### 4.8 유지보수성

- Controller에는 요청/응답 처리만 둔다.
- 핵심 비즈니스 로직은 Service에 둔다.
- Entity에는 잔고 차감, 잔고 증가, 보유 수량 변경처럼 도메인 상태를 바꾸는 메서드를 둘 수 있다.
- DTO를 사용해 Entity를 API 응답으로 직접 노출하지 않는다.
- 패키지는 도메인 기준으로 분리한다.

## 5. 도메인 모델

### 5.1 User

회원 정보를 나타낸다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 사용자 ID |
| email | String | 로그인 이메일 |
| password | String | 암호화된 비밀번호 |
| nickname | String | 닉네임 |
| role | Role | 권한 |
| createdAt | LocalDateTime | 생성 시각 |

### 5.2 Account

사용자의 가상 계좌를 나타낸다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 계좌 ID |
| user | User | 계좌 소유자 |
| cashBalance | Long | 현금 잔고 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

관계:

```text
User 1 : 1 Account
```

### 5.3 Stock

거래 가능한 주식 종목을 나타낸다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 종목 ID |
| stockCode | String | 종목 코드 |
| stockName | String | 종목명 |
| market | String | 시장 구분 |
| currentPrice | Long | 현재가 |
| previousClosePrice | Long | 전일 종가 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

### 5.4 Holding

사용자가 보유한 특정 종목의 상태를 나타낸다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 보유 종목 ID |
| user | User | 사용자 |
| stock | Stock | 종목 |
| quantity | Long | 보유 수량 |
| averagePrice | Long | 평균 매입가 |
| totalPurchaseAmount | Long | 총 매입금액 |
| createdAt | LocalDateTime | 생성 시각 |
| updatedAt | LocalDateTime | 수정 시각 |

관계:

```text
User 1 : N Holding
Stock 1 : N Holding
```

권장 제약:

```text
unique(user_id, stock_id)
```

### 5.5 Trade

매수/매도 거래 내역을 나타낸다.

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 거래 ID |
| user | User | 사용자 |
| stock | Stock | 종목 |
| tradeType | TradeType | BUY 또는 SELL |
| quantity | Long | 거래 수량 |
| price | Long | 거래 가격 |
| totalAmount | Long | 거래 금액 |
| tradedAt | LocalDateTime | 거래 시각 |

관계:

```text
User 1 : N Trade
Stock 1 : N Trade
```

## 6. ERD 요약

```text
User 1 ---- 1 Account

User 1 ---- N Holding
Stock 1 --- N Holding

User 1 ---- N Trade
Stock 1 --- N Trade
```

## 7. 기술 스택 정의서

### 7.1 Backend

| 기술 | 사용 여부 | 목적 |
| --- | --- | --- |
| Java 17 | 필수 | Spring Boot 개발 언어 |
| Spring Boot | 필수 | 애플리케이션 기반 프레임워크 |
| Spring Web | 필수 | REST API 개발 |
| Spring Data JPA | 필수 | ORM 기반 데이터 접근 |
| Spring Security | 필수 | 세션 로그인, 인증/인가 |
| Validation | 필수 | 요청 DTO 검증 |
| Lombok | 선택 | 반복 코드 감소 |
| JUnit 5 | 필수 | 테스트 |
| AssertJ | 필수 | 테스트 검증 |

### 7.2 Database

| 기술 | 사용 여부 | 목적 |
| --- | --- | --- |
| MySQL | 필수 | 운영/개발 DB |
| H2 | 선택 | 테스트 DB |

### 7.3 Optional

| 기술 | 우선순위 | 목적 |
| --- | --- | --- |
| QueryDSL | 선택 | 거래 내역 동적 검색 |
| Scheduler | 선택 | 종목 현재가 랜덤 변동 |
| Redis | 제외 또는 후순위 | 종목 현재가 캐싱 |
| Docker | 제외 또는 후순위 | 배포 환경 구성 |
| JWT | 제외 | 세션 로그인으로 대체 |

### 7.4 추천 패키지 구조

```text
com.example.mockstock
├── global
│   ├── config
│   ├── exception
│   ├── security
│   └── response
├── user
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
├── account
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
├── stock
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
├── holding
│   ├── controller
│   ├── service
│   ├── repository
│   ├── entity
│   └── dto
└── trade
    ├── controller
    ├── service
    ├── repository
    ├── entity
    └── dto
```

## 8. API 명세서

### 8.1 공통 규칙

Base URL:

```text
/api
```

공통 응답 형식:

```json
{
  "success": true,
  "data": {},
  "message": null
}
```

공통 에러 응답 형식:

```json
{
  "success": false,
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

인증:

- 세션 쿠키 기반 인증을 사용한다.
- 로그인 후 발급된 세션 쿠키로 인증이 필요한 API에 접근한다.

### 8.2 회원 API

#### POST /api/auth/signup

회원가입한다.

Request:

```json
{
  "email": "zero@example.com",
  "password": "password1234",
  "nickname": "zero"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "zero@example.com",
    "nickname": "zero"
  },
  "message": "회원가입이 완료되었습니다."
}
```

Errors:

| Status | Code | 설명 |
| --- | --- | --- |
| 400 | `EMAIL_ALREADY_EXISTS` | 이미 가입된 이메일 |
| 400 | `INVALID_REQUEST` | 입력값 검증 실패 |

#### POST /api/auth/login

로그인한다.

Request:

```json
{
  "email": "zero@example.com",
  "password": "password1234"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "zero@example.com",
    "nickname": "zero"
  },
  "message": "로그인되었습니다."
}
```

Errors:

| Status | Code | 설명 |
| --- | --- | --- |
| 401 | `INVALID_LOGIN` | 이메일 또는 비밀번호 불일치 |

#### POST /api/auth/logout

로그아웃한다.

Response:

```json
{
  "success": true,
  "data": null,
  "message": "로그아웃되었습니다."
}
```

#### GET /api/users/me

내 회원 정보를 조회한다.

Response:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "zero@example.com",
    "nickname": "zero",
    "role": "USER"
  },
  "message": null
}
```

### 8.3 종목 API

#### GET /api/stocks

종목 목록을 조회한다.

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| page | N | 페이지 번호 |
| size | N | 페이지 크기 |

Response:

```json
{
  "success": true,
  "data": [
    {
      "stockId": 1,
      "stockCode": "005930",
      "stockName": "삼성전자",
      "market": "KOSPI",
      "currentPrice": 78000
    }
  ],
  "message": null
}
```

#### GET /api/stocks/{stockId}

종목 상세 정보를 조회한다.

Response:

```json
{
  "success": true,
  "data": {
    "stockId": 1,
    "stockCode": "005930",
    "stockName": "삼성전자",
    "market": "KOSPI",
    "currentPrice": 78000,
    "previousClosePrice": 77000
  },
  "message": null
}
```

Errors:

| Status | Code | 설명 |
| --- | --- | --- |
| 404 | `STOCK_NOT_FOUND` | 존재하지 않는 종목 |

#### GET /api/stocks/search

종목명 또는 종목코드로 검색한다.

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| keyword | Y | 검색어 |

Example:

```http
GET /api/stocks/search?keyword=삼성
```

### 8.4 계좌 API

#### GET /api/accounts/me

내 계좌 요약 정보를 조회한다.

Response:

```json
{
  "success": true,
  "data": {
    "cashBalance": 7500000,
    "stockEvaluationAmount": 2800000,
    "totalPurchaseAmount": 2500000,
    "totalAssetAmount": 10300000,
    "profitAmount": 300000,
    "profitRate": 12.0
  },
  "message": null
}
```

### 8.5 보유 종목 API

#### GET /api/holdings/me

내 보유 종목 목록을 조회한다.

Response:

```json
{
  "success": true,
  "data": [
    {
      "stockId": 1,
      "stockCode": "005930",
      "stockName": "삼성전자",
      "quantity": 10,
      "averagePrice": 75000,
      "currentPrice": 78000,
      "totalPurchaseAmount": 750000,
      "evaluationAmount": 780000,
      "profitAmount": 30000,
      "profitRate": 4.0
    }
  ],
  "message": null
}
```

### 8.6 거래 API

#### POST /api/trades/buy

주식을 매수한다.

Request:

```json
{
  "stockId": 1,
  "quantity": 10
}
```

Response:

```json
{
  "success": true,
  "data": {
    "tradeId": 1,
    "tradeType": "BUY",
    "stockId": 1,
    "stockName": "삼성전자",
    "quantity": 10,
    "price": 78000,
    "totalAmount": 780000,
    "tradedAt": "2026-06-08T10:30:00"
  },
  "message": "매수가 완료되었습니다."
}
```

Errors:

| Status | Code | 설명 |
| --- | --- | --- |
| 400 | `INVALID_QUANTITY` | 수량이 1보다 작음 |
| 400 | `INSUFFICIENT_CASH` | 현금 잔고 부족 |
| 404 | `STOCK_NOT_FOUND` | 존재하지 않는 종목 |

#### POST /api/trades/sell

주식을 매도한다.

Request:

```json
{
  "stockId": 1,
  "quantity": 5
}
```

Response:

```json
{
  "success": true,
  "data": {
    "tradeId": 2,
    "tradeType": "SELL",
    "stockId": 1,
    "stockName": "삼성전자",
    "quantity": 5,
    "price": 78000,
    "totalAmount": 390000,
    "tradedAt": "2026-06-08T10:45:00"
  },
  "message": "매도가 완료되었습니다."
}
```

Errors:

| Status | Code | 설명 |
| --- | --- | --- |
| 400 | `INVALID_QUANTITY` | 수량이 1보다 작음 |
| 400 | `INSUFFICIENT_QUANTITY` | 보유 수량 부족 |
| 404 | `STOCK_NOT_FOUND` | 존재하지 않는 종목 |
| 404 | `HOLDING_NOT_FOUND` | 보유하지 않은 종목 |

### 8.7 거래 내역 API

#### GET /api/trades/me

내 거래 내역을 조회한다.

Query Parameters:

| 이름 | 필수 | 설명 |
| --- | --- | --- |
| type | N | `BUY` 또는 `SELL` |
| stockId | N | 종목 ID |
| startDate | N | 조회 시작일, `yyyy-MM-dd` |
| endDate | N | 조회 종료일, `yyyy-MM-dd` |
| page | N | 페이지 번호 |
| size | N | 페이지 크기 |

Example:

```http
GET /api/trades/me?type=BUY&stockId=1&startDate=2026-06-01&endDate=2026-06-08
```

Response:

```json
{
  "success": true,
  "data": [
    {
      "tradeId": 1,
      "tradeType": "BUY",
      "stockId": 1,
      "stockName": "삼성전자",
      "quantity": 10,
      "price": 78000,
      "totalAmount": 780000,
      "tradedAt": "2026-06-08T10:30:00"
    }
  ],
  "message": null
}
```

## 9. 구현 우선순위

### 9.1 1주차

1. 프로젝트 생성
2. User, Account, Stock 엔티티 생성
3. 회원가입 구현
4. 계좌 자동 생성 구현
5. Spring Security 세션 로그인 구현
6. 초기 종목 데이터 등록
7. 종목 목록/상세 조회 구현

### 9.2 2주차

1. Holding, Trade 엔티티 생성
2. 매수 기능 구현
3. 매도 기능 구현
4. 보유 종목 조회 구현
5. 계좌 요약 조회 구현
6. 거래 내역 조회 구현
7. 주요 예외 처리 구현

### 9.3 3주차

1. 매수/매도 테스트 작성
2. 평균 매입가 계산 테스트 작성
3. 포트폴리오 수익률 계산 테스트 작성
4. 동시성 테스트 작성
5. 계좌 조회 락 적용
6. 거래 내역 조건 검색 보완
7. README 및 발표 자료 정리

## 10. 포트폴리오 강조 포인트

이 프로젝트는 단순 CRUD보다 다음 내용을 강조한다.

- 매수/매도 시 계좌 잔고, 보유 수량, 거래 내역의 정합성을 보장했다.
- 거래 처리를 하나의 트랜잭션으로 묶어 중간 실패 시 전체 롤백되도록 했다.
- 같은 사용자의 동시 주문으로 잔고나 보유 수량이 음수가 되는 문제를 고려했다.
- 계좌 조회에 비관적 락을 적용해 동시성 문제를 방지했다.
- 보유 종목의 평균 매입가, 평가손익, 수익률을 계산해 포트폴리오 조회 API를 구현했다.

README에 사용할 수 있는 문장:

> 가상 주식 거래 서비스에서 매수/매도 시 계좌 잔고, 보유 수량, 거래 내역의 데이터 정합성을 보장하기 위해 `@Transactional`을 적용했습니다. 또한 동일 사용자가 동시에 여러 주문을 요청할 때 잔고나 보유 수량이 잘못 계산되는 문제를 방지하기 위해 계좌 단위의 비관적 락을 적용했습니다.
