package org.example.virtuber.stock.controller;

import lombok.RequiredArgsConstructor;
import org.example.virtuber.common.response.ApiResponse;
import org.example.virtuber.stock.dto.StockPriceHistoryResponse;
import org.example.virtuber.stock.dto.StockResponse;
import org.example.virtuber.stock.service.StockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stocks")
public class StockController {

  private final StockService stockService;

  @GetMapping
  public ApiResponse<List<StockResponse>> getStocks() {
    return ApiResponse.ok(
        stockService.getStocks(),
        "종목 목록 조회에 성공했습니다."
    );
  }

  @GetMapping("/{stockId}/price-histories")
  public ApiResponse<List<StockPriceHistoryResponse>> getLatestPriceHistories(
      @PathVariable Long stockId
  ) {
    return ApiResponse.ok(
        stockService.getLatestPriceHistories(stockId),
        "주가 기록 조회에 성공했습니다."
    );
  }
}