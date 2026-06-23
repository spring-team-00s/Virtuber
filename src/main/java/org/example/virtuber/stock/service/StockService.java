package org.example.virtuber.stock.service;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.stock.dto.StockPriceHistoryResponse;
import org.example.virtuber.stock.dto.StockResponse;
import org.example.virtuber.stock.entity.StockPriceHistory;
import org.example.virtuber.stock.repository.StockPriceHistoryRepository;
import org.example.virtuber.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

  private final StockRepository stockRepository;
  private final StockPriceHistoryRepository stockPriceHistoryRepository;

  public List<StockResponse> getStocks() {
    return stockRepository.findAll()
        .stream()
        .map(StockResponse::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<StockPriceHistoryResponse> getLatestPriceHistories(Long stockId) {
    if(!stockRepository.existsById(stockId)) {
      throw new BusinessException(ErrorCode.STOCK_NOT_FOUND);
    }
    List<StockPriceHistory> latestHistories =
        stockPriceHistoryRepository.findTop20ByStock_IdOrderByRecordedTimeDesc(stockId);

    List<StockPriceHistory> sortedHistories = new ArrayList<>(latestHistories);
    Collections.reverse(sortedHistories);

    return sortedHistories.stream()
        .map(StockPriceHistoryResponse::from)
        .toList();
  }
}
