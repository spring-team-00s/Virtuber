# 주식 모의투자 웹 애플리케이션 MVP 요구사항 정의서

작성일: 2026-06-16

## 1. 프로젝트 개요

### 1.1 프로젝트명

Spring 기반 주식 모의투자 웹 애플리케이션

### 1.2 프로젝트 목적

사용자가 회원가입 시 가상 계좌와 시드머니를 지급받고, 미리 등록된 주식 종목을 조회한 뒤 매수/매도할 수 있는 모의투자 서비스를 구현한다.

이번 MVP에서는 기능을 작게 유지하기 위해 API를 다음 흐름에만 집중한다.

```text
회원가입
-> 로그인
-> 계좌 조회
-> 주식 정보 조회
-> 매수/매도
-> 계좌 조회로 결과 확인
-> 계좌 초기화
```

거래 내역은 내부적으로 저장하되, 별도 거래 내역 조회 API는 MVP 범위에서 제외한다.

### 1.3 핵심 학습 목표

- Spring Boot 기반 REST API 설계
- Spring Security 기반 JWT 인증 구현
- 회원가입 시 계좌 자동 생성
- JPA 기반 User, Account, Stock, Holding, Trade 도메인 설계
- 매수/매도 시 잔고, 보유 수량, 거래 내역의 정합성 보장
- `@Transactional`을 이용한 거래 처리
- 계좌 초기화 기능 구현

### 1.4 MVP 기능 범위

필수 기능:

- 회원가입
- 로그인 / 로그아웃
- 계좌 조회
- 보유 주식 조회
- 계좌 초기화
- 주식 정보 조회
- 매수
- 매도

제외 기능:

- 별도 회원 정보 조회 API
- 별도 보유 종목 조회 API
- 별도 거래 내역 조회 API
- 종목 상세 조회 API
- 종목 검색 API
- 수익률 랭킹
- 관심 종목
- 실제 주식 API 연동
- 실시간 시세
- 차트
- 호가창
- 지정가/시장가 주문
- OAuth 인증

## 2. 확정 API 목록

MVP에서 제공하는 API는 아래 8개로 제한한다.

```http
POST /api/v1/auth/signup
POST /api/v1/auth/signin
POST /api/v1/auth/signout

GET  /api/v1/accounts/me
PUT  /api/v1/accounts/me/init

GET  /api/v1/stocks

POST /api/v1/trades/buy
POST /api/v1/trades/sell
```

## 3. 사용자 및 권한

### 3.1 사용자 유형

| 사용자 | 설명 |
| --- | --- |
| 비회원 | 회원가입, 로그인, 주식 정보 조회 가능 |
| 회원 | 계좌 조회, 계좌 초기화, 매수, 매도 가능 |

### 3.2 인증 방식

- Spring Security JWT 로그인을 사용한다.
- 로그인 성공 시 Access Token과 Refresh Token을 발급한다.
- 비밀번호는 `PasswordEncoder`를 사용해 암호화한다.

### 3.3 인가 규칙

- 로그인하지 않은 사용자는 계좌 조회, 계좌 초기화, 매수, 매도 API에 접근할 수 없다.
- 사용자는 본인의 계좌만 조회할 수 있다.
- 사용자는 본인의 계좌와 보유 주식만 초기화할 수 있다.
- 사용자는 본인의 계좌 잔고와 보유 주식만 매수/매도 로직에 사용할 수 있다.
- 클라이언트가 전달한 userId를 신뢰하지 않고, 현재 로그인한 사용자 정보를 기준으로 처리한다.

## 4. 기능 요구사항

### 4.1 회원가입

#### FR-AUTH-001 회원가입

- 사용자는 ID, 비밀번호를 입력해 회원가입할 수 있다.
- ID는 중복될 수 없다.
- 비밀번호는 암호화하여 저장한다.
- 회원가입 성공 시 사용자 계좌를 자동 생성한다.
- 생성된 계좌에는 시드머니 `10,000,000원`을 지급한다.

처리 흐름:

```text
회원가입 요청
-> ID 중복 검사
-> 비밀번호 암호화
-> 사용자 저장
-> 계좌 생성
-> 시드머니 지급
```

### 4.2 로그인 / 로그아웃

#### FR-AUTH-002 로그인

- 사용자는 ID와 비밀번호로 로그인할 수 있다.
- 로그인 성공 시 Access/Refresh Token이 발급된다.
- 로그인 실패 시 실패 응답을 반환한다.

#### FR-AUTH-003 로그아웃

- 로그인한 사용자는 로그아웃할 수 있다.
- 로그아웃 시 Refresh Token을 삭제한다.

### 4.3 계좌 조회

#### FR-ACCOUNT-001 내 계좌 조회

- 로그인한 사용자는 본인의 계좌 정보를 조회할 수 있다.
- 계좌 조회 응답에는 계좌 요약과 보유 주식 목록을 함께 포함한다.
- 별도 보유 주식 조회 API는 만들지 않는다.

응답 항목:

- 현금 잔고
- 총 매입금액
- 주식 평가금액
- 총 자산
- 평가손익
- 수익률
- 보유 주식 목록

계산식:

```text
주식 평가금액 = 각 보유 주식의 현재가 * 보유수량 합계
총 자산 = 현금 잔고 + 주식 평가금액
평가손익 = 주식 평가금액 - 총 매입금액
수익률 = 평가손익 / 총 매입금액 * 100
```

단, 총 매입금액이 0이면 수익률은 0으로 처리한다.

보유 주식 항목:

- 종목 ID
- 종목 코드
- 종목명
- 보유 수량
- 평균 매입가
- 현재가
- 총 매입금액
- 평가금액
- 평가손익
- 수익률

### 4.4 계좌 초기화

#### FR-ACCOUNT-002 계좌 초기화

- 로그인한 사용자는 본인의 계좌를 초기 상태로 되돌릴 수 있다.
- 초기화 시 현금 잔고는 시드머니 `10,000,000원`으로 복구한다.
- 사용자 비밀번호를 Request Body로 받아 재확인한다.
- 사용자의 보유 주식 정보를 삭제한다.
- 사용자의 거래 내역을 삭제한다.
- 다른 사용자의 계좌, 보유 주식, 거래 내역은 영향을 받지 않는다.

처리 흐름:

```text
초기화 요청
-> 현재 로그인 사용자 확인
-> 비밀번호 재확인
-> 해당 사용자의 Holding 삭제
-> 해당 사용자의 Trade 삭제
-> Account.cashBalance를 10,000,000원으로 변경
```

초기화는 여러 데이터를 함께 변경하므로 하나의 트랜잭션으로 처리한다.

### 4.5 주식 정보 조회

#### FR-STOCK-001 초기 주식 데이터 등록

- 주식 데이터는 실제 API를 연동하지 않고 DB에 미리 등록한다.
- 미래 가격 데이터를 저장하지 않는다.
- 거래 기준이 되는 현재가를 가진 종목 기본 정보만 저장한다.

초기 종목 예시:

| 종목코드 | 종목명 | 현재가 | 상한가 또는 상승 기준 가격 | 하한가 또는 하락 기준 가격 |
| --- | --- | ---: | ---: | ---: |
| 005930 | 삼성전자 | 78,000 | 101,400 | 54,600 |
| 000660 | SK하이닉스 | 180,000 | 234,000 | 126,000 |
| 035420 | NAVER | 180,000 | 234,000 | 126,000 |
| 035720 | 카카오 | 45,000 | 58,500 | 31,500 |
| 005380 | 현대차 | 250,000 | 325,000 | 175,000 |
| 373220 | LG에너지솔루션 | 350,000 | 455,000 | 245,000 |

#### FR-STOCK-002 주식 목록 조회

- 사용자는 등록된 주식 목록을 조회할 수 있다.
- MVP에서는 목록 조회 API만 제공한다.
- 종목 상세 조회와 검색 API는 제외한다.

응답 항목:

- 종목 ID
- 종목 코드
- 종목명
- 현재가
- 상한가 또는 상승 기준 가격
- 하한가 또는 하락 기준 가격
- 회사 정보
- 재무 정보

### 4.6 매수

#### FR-TRADE-001 매수 요청

- 로그인한 사용자는 종목 ID와 수량을 입력해 매수할 수 있다.
- 매수 수량은 1 이상이어야 한다.
- 매수 가격은 요청 시점의 종목 현재가를 기준으로 한다.
- 주문 금액은 `현재가 * 수량`으로 계산한다.

#### FR-TRADE-002 매수 검증

- 존재하지 않는 종목은 매수할 수 없다.
- 현금 잔고가 주문 금액보다 부족하면 매수는 실패한다.
- 실패한 매수는 계좌, 보유 주식, 거래 내역에 어떤 변경도 남기지 않는다.

#### FR-TRADE-003 매수 성공 처리

매수 성공 시 다음 처리를 하나의 트랜잭션으로 수행한다.

1. 계좌 현금 잔고 차감
2. 보유 주식 생성 또는 수량 증가
3. 평균 매입가 재계산
4. 거래 내역 저장

평균 매입가 계산식:

```text
새 평균 매입가 = (기존 총 매입금액 + 추가 매입금액) / 총 보유수량
```

### 4.7 매도

#### FR-TRADE-004 매도 요청

- 로그인한 사용자는 보유 중인 종목을 매도할 수 있다.
- 매도 요청에는 종목 ID와 수량이 포함된다.
- 매도 수량은 1 이상이어야 한다.
- 매도 가격은 요청 시점의 종목 현재가를 기준으로 한다.

#### FR-TRADE-005 매도 검증

- 존재하지 않는 종목은 매도할 수 없다.
- 보유하지 않은 종목은 매도할 수 없다.
- 보유 수량보다 많은 수량은 매도할 수 없다.
- 실패한 매도는 계좌, 보유 주식, 거래 내역에 어떤 변경도 남기지 않는다.

#### FR-TRADE-006 매도 성공 처리

매도 성공 시 다음 처리를 하나의 트랜잭션으로 수행한다.

1. 보유 수량 감소
2. 계좌 현금 잔고 증가
3. 거래 내역 저장
4. 보유 수량이 0이면 보유 주식 삭제

## 5. 비기능 요구사항

### 5.1 트랜잭션 정합성

- 매수, 매도, 계좌 초기화는 반드시 하나의 트랜잭션으로 처리한다.
- 중간 단계에서 예외가 발생하면 전체 작업을 롤백한다.
- 현금 잔고 변경, 보유 주식 변경, 거래 내역 저장 중 일부만 반영되는 상태를 허용하지 않는다.

적용 대상:

- `POST /api/v1/trades/buy`
- `POST /api/v1/trades/sell`
- `PUT /api/v1/accounts/me/init`

### 5.2 동시성 제어

- 같은 사용자가 동시에 여러 매수 요청을 보내도 잔고가 음수가 되면 안 된다.
- 같은 사용자가 동시에 여러 매도 요청을 보내도 보유 수량이 음수가 되면 안 된다.
- 계좌 데이터를 변경하는 매수/매도/초기화 요청은 계좌 단위로 순차 처리되도록 한다.

권장 구현:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select a from Account a where a.user.id = :userId")
Optional<Account> findByUserIdForUpdate(Long userId);
```

### 5.3 보안

- 비밀번호는 안전한 해시 알고리즘으로 암호화한다.
- 인증이 필요한 API는 Spring Security 설정으로 보호한다.
- 응답 DTO에는 비밀번호를 포함하지 않는다.
- 서버 로그에 비밀번호, Access Token, Refresh Token 같은 민감 정보를 출력하지 않는다.
- 사용자 식별은 요청 파라미터가 아니라 인증 객체에서 가져온다.

### 5.4 데이터 검증

- ID는 필수 값이다.
- 비밀번호는 필수 값이며 최소 길이를 만족해야 한다.
- 매수/매도 수량은 1 이상이어야 한다.
- 존재하지 않는 종목으로 거래할 수 없다.
- 잔고보다 큰 금액은 매수할 수 없다.
- 보유 수량보다 많은 수량은 매도할 수 없다.

### 5.5 예외 처리

공통 에러 응답 형식을 사용한다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "TRADE_001",
    "message": "현금 잔고가 부족합니다.",
    "details": []
  },
  "timestamp": "2026-06-16T10:30:00"
}
```

요청값 검증 실패처럼 필드별 상세 사유가 필요한 경우 `error.details`에 필드명과 사유를 포함한다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "CMN_002",
    "message": "입력값이 올바르지 않습니다.",
    "details": [
      {
        "field": "quantity",
        "reason": "1 이상이어야 합니다."
      }
    ]
  },
  "timestamp": "2026-06-16T10:30:00"
}
```

주요 에러 코드:

| 코드 | 설명 |
| --- | --- |
| `AUTH_001` | 이미 사용 중인 ID |
| `AUTH_002` | ID 또는 비밀번호 불일치 |
| `AUTH_003` | 인증되지 않은 사용자 |
| `AUTH_005` | 유효하지 않은 토큰 |
| `USER_001` | 존재하지 않는 사용자 |
| `STOCK_001` | 존재하지 않는 종목 |
| `TRADE_001` | 현금 잔고 부족 |
| `TRADE_002` | 매도 수량이 보유 수량을 초과 |
| `CMN_001` | 접근 권한 없음 |
| `CMN_002` | 입력값 검증 실패 |
| `CMN_500` | 서버 내부 오류 |

### 5.6 테스트

필수 테스트:

- 회원가입 성공 시 계좌 자동 생성
- 로그인 성공/실패
- 계좌 조회 시 보유 주식 포함
- 계좌 초기화 시 시드머니 복구
- 계좌 초기화 시 보유 주식 삭제
- 계좌 초기화 시 거래 내역 삭제
- 매수 성공
- 잔고 부족으로 매수 실패
- 추가 매수 시 평균 매입가 계산
- 매도 성공
- 보유 수량 부족으로 매도 실패
- 매도 후 보유 수량이 0이면 Holding 삭제
- 동시 매수 요청 시 잔고 초과 방지
- 동시 매도 요청 시 보유 수량 초과 방지

## 6. 도메인 모델

도메인 모델은 현재 구현된 엔티티 코드를 기준으로 정의한다.

### 6.1 User

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 사용자 ID |
| userId | String | 로그인 ID |
| password | String | 암호화된 비밀번호 |

제약:

```text
unique(user_id)
```

### 6.2 Account

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 계좌 ID |
| user | User | 계좌 소유자 |
| cashBalance | Long | 현금 잔고 |
| seedMoney | Long | 초기화 기준 시드머니 |

관계:

```text
User 1 : 1 Account
```

### 6.3 Stock

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 종목 ID |
| stockCode | String | 종목 코드 |
| stockName | String | 종목명 |
| currentPrice | Long | 현재가 |
| upPrice | Long | 상한가 또는 상승 기준 가격 |
| lowPrice | Long | 하한가 또는 하락 기준 가격 |
| companyInfo | String | 회사 정보 |
| financialInfo | String | 재무 정보 |

제약:

```text
unique(stock_code)
unique(stock_name)
```

### 6.4 Holding

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 보유 주식 ID |
| account | Account | 보유 계좌 |
| stock | Stock | 종목 |
| quantity | Long | 보유 수량 |
| averagePrice | Long | 평균 매입가 |

제약:

```text
unique(account_id, stock_id)
```

### 6.5 Trade

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 거래 ID |
| account | Account | 거래 계좌 |
| stock | Stock | 종목 |
| tradeType | TradeType | BUY 또는 SELL |
| quantity | Long | 거래 수량 |
| price | Long | 거래 가격 |
| tradedTime | LocalDateTime | 거래 시각 |

거래 내역 조회 API는 제공하지 않지만, 매수/매도 이력 저장과 초기화 처리를 위해 Trade 엔티티는 유지한다.

### 6.6 StockPriceHistory

| 필드 | 타입 | 설명 |
| --- | --- | --- |
| id | Long | 주가 이력 ID |
| stock | Stock | 종목 |
| price | Long | 기록 가격 |
| recordedTime | LocalDateTime | 기록 시각 |

인덱스:

```text
idx_stock_price_histories_stock_time(stock_id, recorded_time)
```

## 7. 기술 스택

| 영역 | 기술 | 사용 목적 |
| --- | --- | --- |
| Language | Java 17 | 백엔드 개발 언어 |
| Framework | Spring Boot | 애플리케이션 기반 |
| Web | Spring Web | REST API 구현 |
| ORM | Spring Data JPA | DB 접근 |
| Security | Spring Security, JWT | 토큰 기반 인증/인가 |
| Validation | Bean Validation | 요청값 검증 |
| Database | MySQL | 개발/운영 DB |
| Test | JUnit 5, AssertJ | 테스트 |
| Optional | H2 | 테스트 DB |
| Optional | Lombok | 반복 코드 감소 |

MVP 제외 기술:

- OAuth
- QueryDSL
- Redis
- Docker
- 외부 주식 API

## 8. API 명세

### 8.1 공통 규칙

Base URL:

```text
/api/v1
```

공통 성공 응답:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

공통 실패 응답:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": []
  },
  "timestamp": "2026-06-16T10:30:00"
}
```

현재 공통 응답 객체는 별도의 `message` 필드를 제공하지 않는다. 성공 안내 문구가 필요한 경우에도 응답 본문은 `success`, `data`, `error`, `timestamp` 구조를 따른다.

### 8.2 POST /api/v1/auth/signup

회원가입한다.

Request:

```json
{
  "userId": "zero",
  "password": "password1234"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": "zero",
    "cashBalance": 10000000
  },
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.3 POST /api/v1/auth/signin

로그인한다.

Request:

```json
{
  "userId": "zero",
  "password": "password1234"
}
```

Response:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": "zero"
  },
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.4 POST /api/v1/auth/signout

로그아웃한다.

Response:

```json
{
  "success": true,
  "data": null,
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.5 GET /api/v1/accounts/me

내 계좌와 보유 주식을 조회한다.

Response:

```json
{
  "success": true,
  "data": {
    "cashBalance": 7500000,
    "totalPurchaseAmount": 2500000,
    "stockEvaluationAmount": 2800000,
    "totalAssetAmount": 10300000,
    "profitAmount": 300000,
    "profitRate": 12.0,
    "holdings": [
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
    ]
  },
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.6 PUT /api/v1/accounts/me/init

내 계좌를 초기화한다.

Response:

```json
{
  "success": true,
  "data": {
    "cashBalance": 10000000,
    "holdingsCount": 0
  },
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.7 GET /api/v1/stocks

주식 목록을 조회한다.

Response:

```json
{
  "success": true,
  "data": [
    {
      "stockId": 1,
      "stockCode": "005930",
      "stockName": "삼성전자",
      "currentPrice": 78000,
      "upPrice": 101400,
      "lowPrice": 54600,
      "companyInfo": "전자 제품 및 반도체 제조 기업",
      "financialInfo": "재무 정보 요약"
    }
  ],
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.8 POST /api/v1/trades/buy

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
    "cashBalance": 9220000,
    "tradedTime": "2026-06-16T10:30:00"
  },
  "error": null,
  "timestamp": "2026-06-16T10:30:00"
}
```

### 8.9 POST /api/v1/trades/sell

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
    "cashBalance": 9610000,
    "tradedTime": "2026-06-16T10:45:00"
  },
  "error": null,
  "timestamp": "2026-06-16T10:45:00"
}
```

## 9. 구현 우선순위

### 9.1 1단계

1. 프로젝트 생성
2. User, Account 엔티티 생성
3. 회원가입 구현
4. 회원가입 시 계좌 자동 생성
5. Spring Security JWT 로그인 구현
6. 로그아웃 구현

### 9.2 2단계

1. Stock 엔티티 생성
2. 초기 주식 데이터 등록
3. 주식 목록 조회 구현
4. Holding, Trade 엔티티 생성
5. 계좌 조회 응답에 보유 주식 포함

### 9.3 3단계

1. 매수 구현
2. 매도 구현
3. 계좌 초기화 구현
4. 트랜잭션 적용
5. 주요 테스트 작성
6. 동시성 테스트 및 계좌 락 적용

## 10. 포트폴리오 강조 포인트

이 프로젝트는 작은 API 범위 안에서 다음 내용을 강조한다.

- 회원가입 시 사용자 계좌와 시드머니가 자동 생성된다.
- 계좌 조회 API 하나로 현금, 총 자산, 수익률, 보유 주식까지 확인할 수 있다.
- 매수/매도 시 계좌 잔고, 보유 수량, 거래 내역의 정합성을 보장한다.
- 거래 처리를 하나의 트랜잭션으로 묶어 중간 실패 시 전체 롤백되도록 한다.
- 계좌 초기화 기능으로 테스트와 시연을 반복하기 쉽게 만든다.
- 동시 주문으로 잔고나 보유 수량이 잘못 계산되는 문제를 고려한다.

README용 문장:

> API 범위를 회원가입, 로그인, 계좌 조회, 주식 조회, 매수/매도, 계좌 초기화로 제한해 MVP를 작게 유지했습니다. 매수/매도와 초기화는 하나의 트랜잭션으로 처리하여 계좌 잔고, 보유 주식, 거래 내역의 정합성을 보장했습니다.
