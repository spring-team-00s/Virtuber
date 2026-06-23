package org.example.virtuber.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.virtuber.stock.entity.StockPriceHistory;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StockPriceHistoryResponse {

  private Long historyId;
  private Long stockId;
  private Long price;
  private LocalDateTime recordedTime;

  public static StockPriceHistoryResponse from(StockPriceHistory history) {
    return new StockPriceHistoryResponse(
        history.getId(),
        history.getStock().getId(),
        history.getPrice(),
        history.getRecordedTime()
    );
  }
}
