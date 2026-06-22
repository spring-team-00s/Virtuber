package org.example.virtuber.trade.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.trade.entity.Trade;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradeResponse {
    private Long tradeId;
    private String tradeType;
    private Long stockId;
    private String stockName;
    private Long quantity;
    private Long price;
    private Long totalAmount;
    private Long cashBalance;
    private Long averagePriceBeforeSell;
    private Long sellProfitAmount;
    private Double sellProfitRate;
    private LocalDateTime tradedTime;




    public static TradeResponse from(Trade trade, Account account) {
        return new TradeResponse(
                trade.getId(),
                trade.getTradeType().name(),
                trade.getStock().getId(),
                trade.getStock().getStockName(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getPrice() * trade.getQuantity(),
                account.getCashBalance(),
                trade.getAveragePriceBeforeSell(),
                trade.getSellProfitAmount(),
                trade.getSellProfitRate(),
                trade.getTradedTime()
        );
    }
}
