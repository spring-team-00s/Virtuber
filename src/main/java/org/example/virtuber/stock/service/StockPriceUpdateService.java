package org.example.virtuber.stock.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.virtuber.stock.entity.Stock;
import org.example.virtuber.stock.entity.StockPriceHistory;
import org.example.virtuber.stock.repository.StockPriceHistoryRepository;
import org.example.virtuber.stock.repository.StockRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockPriceUpdateService {

  private final StockRepository stockRepository;
  private final StockPriceHistoryRepository stockPriceHistoryRepository;

  @Value("${stock.price-update.min-rate:-0.03}")
  private double minRate;

  @Value("${stock.price-update.max-rate:0.03}")
  private double maxRate;

  @Transactional
  public void updateAllStockPrices() {
    List<Stock> stocks = stockRepository.findAll();

    if(stocks.isEmpty()) {
      log.info("가격을 변경할 대상 주식이 없습니다.");
      return;
    }

    LocalDateTime recordTime = LocalDateTime.now();
    List<StockPriceHistory> histories = new ArrayList<>();

    for(Stock stock : stocks) {
      // 기존 가격 저장
      Long oldPrice = stock.getCurrentPrice();
      // 변경 가격 계산
      Long newPrice = calculateNextPrice(stock);

      // 현재가 변경
      stock.changePrice(newPrice);

      // 히스토리 기록 및 리스트
      histories.add(
          StockPriceHistory.record(stock, newPrice, recordTime)
      );

      log.info(
          "주식 가격 변경 - {}({}): {} -> {}",
          stock.getStockName(),
          stock.getStockCode(),
          oldPrice,
          newPrice
      );
    }

    stockPriceHistoryRepository.saveAll(histories);
  }

  // 주식 다음 가격 계산
  private Long calculateNextPrice(Stock stock) {
    double randomRate = ThreadLocalRandom.current()
        .nextDouble(minRate, maxRate);

    // 변경 가격 계산
    long calculatePrice = Math.round(stock.getCurrentPrice() * (1 + randomRate));

    // 반올림 (10원단위)
    calculatePrice = roundToNearestTen(calculatePrice);

    // 하한가 / 상한가 보정
    calculatePrice = Math.max(calculatePrice, stock.getLowPrice());
    calculatePrice = Math.min(calculatePrice, stock.getUpPrice());

    // 최소가 보장
    return Math.max(calculatePrice, 1L);
  }

  private long roundToNearestTen(long price) {
    return Math.round(price / 10.0) * 10;
  }


}
