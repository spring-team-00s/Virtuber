package org.example.virtuber.common.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.virtuber.common.exception.ErrorCode;

import java.util.List;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {
  private String code;
  private String message;
  private List<ErrorDetail> details;

  public static ErrorResponse of(ErrorCode errorCode) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getMessage(),
        List.of()
    );
  }

  public static ErrorResponse of(ErrorCode errorCode, List<ErrorDetail> details) {
    return new ErrorResponse(
        errorCode.getCode(),
        errorCode.getMessage(),
        details
    );
  }
}
