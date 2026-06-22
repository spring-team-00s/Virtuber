package org.example.virtuber.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // AUTH
  DUPLICATE_ID(HttpStatus.CONFLICT, "AUTH_001", "이미 사용 중인 ID입니다."),
  INVALID_CREDENTIALS(HttpStatus.CONFLICT, "AUTH_002", "ID 또는 비밀번호가 올바르지 않습니다."),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_003", "로그인이 필요합니다."),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "유효하지 않은 토큰입니다."),

  // USER
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "존재하지 않는 사용자입니다."),

  // ACCOUNT
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCOUNT_001", "존재하지 않는 계좌입니다."),

  // STOCK
  STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "STOCK_001", "존재하지 않는 종목입니다."),

  // TRADE
  INSUFFICIENT_CASH(HttpStatus.BAD_REQUEST, "TRADE_001", "현금 잔고가 부족합니다."),
  INSUFFICIENT_HOLDING(HttpStatus.BAD_REQUEST, "TRADE_002", "매도 수량이 보유 수량을 초과하였습니다."),

  // COMMON
  FORBIDDEN(HttpStatus.FORBIDDEN, "CMN_001", "접근 권한이 없습니다."),
  INVALID_INPUT(HttpStatus.BAD_REQUEST, "CMN_002", "입력값이 올바르지 않습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CMN_500", "서버 오류가 발생했습니다.");

  private final HttpStatus status;
  private final String code;
  private final String message;
}
