package org.example.virtuber.stock.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.virtuber.stock.service.StockPriceUpdateService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockPriceScheduler {

  private final StockPriceUpdateService stockPriceUpdateService;

  @Scheduled(
      fixedDelayString = "${stock.price-update.fixed-delay-ms:10000}",
      initialDelayString = "${stock.price-update.initial-delay-ms:5000}"
  )
  public void updateStockPrices() {
    log.info("주식 가격 변동 스케줄러 시작");
    stockPriceUpdateService.updateAllStockPrices();
    log.info("주식 가격 변동 스케줄러 종료");
  }
}
