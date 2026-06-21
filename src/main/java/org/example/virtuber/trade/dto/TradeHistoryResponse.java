package org.example.virtuber.trade.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.virtuber.trade.entity.Trade;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TradeHistoryResponse {

    private Long tradeId;
    private String tradeType;
    private Long stockId;
    private String stockCode;
    private String stockName;
    private Long quantity;
    private Long price;
    private Long totalAmount;
    private LocalDateTime tradedTime;

    public static TradeHistoryResponse from(Trade trade) {
        return new TradeHistoryResponse(
                trade.getId(),
                trade.getTradeType().name(),
                trade.getStock().getId(),
                trade.getStock().getStockCode(),
                trade.getStock().getStockName(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getPrice() * trade.getQuantity(),
                trade.getTradedTime()
        );
    }
}
