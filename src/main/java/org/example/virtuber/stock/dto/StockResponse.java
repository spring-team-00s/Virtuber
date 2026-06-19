package org.example.virtuber.stock.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.virtuber.stock.entity.Stock;

@Getter
@AllArgsConstructor
public class StockResponse {

  private Long stockId;
  private String stockCode;
  private String stockName;
  private Long currentPrice;
  private Long upPrice;
  private Long lowPrice;
  private String companyInfo;
  private String financialInfo;

  public static StockResponse from(Stock stock) {
    return new StockResponse(
        stock.getId(),
        stock.getStockCode(),
        stock.getStockName(),
        stock.getCurrentPrice(),
        stock.getUpPrice(),
        stock.getLowPrice(),
        stock.getCompanyInfo(),
        stock.getFinancialInfo()
    );
  }
}