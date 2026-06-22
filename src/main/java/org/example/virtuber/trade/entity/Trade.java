package org.example.virtuber.trade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.stock.entity.Stock;

import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // trades.account_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // trades.stock_id -> stocks.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    @Column(name = "trade_type", nullable = false, length = 20)
    private TradeType tradeType;

    @Column(nullable = false)
    private Long quantity;

    // 거래 당시 가격
    @Column(nullable = false)
    private Long price;

    @Column(name = "traded_time", nullable = false)
    private LocalDateTime tradedTime;

    // 매도 당시 판매 전 보유 평단가. 매수 거래는 null.
    @Column(name = "average_price_before_sell")
    private Long averagePriceBeforeSell;

    // 매도 실현손익. (매도가 - 판매 전 평균가) * 매도수량. 매수 거래는 null.
    @Column(name = "sell_profit_amount")
    private Long sellProfitAmount;

    // 매도 수익률. ((매도가 - 판매 전 평균가) / 판매 전 평균가) * 100. 매수 거래는 null.
    @Column(name = "sell_profit_rate")
    private Double sellProfitRate;

    // 거래내역 생성자
    private Trade(Account account, Stock stock, TradeType tradeType, Long quantity, Long price, Long averagePriceBeforeSell, Long sellProfitAmount, Double sellProfitRate) {
        this.account = account;
        this.stock = stock;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = price;
        this.averagePriceBeforeSell = averagePriceBeforeSell;
        this.sellProfitAmount = sellProfitAmount;
        this.sellProfitRate = sellProfitRate;
        this.tradedTime = LocalDateTime.now();
    }
    // 매수 거래 생성
    public static Trade buy(Account account, Stock stock, Long quantity, Long price) {
        return new Trade(account, stock, TradeType.BUY, quantity, price, null, null, null);
    }
    // 매도 거래 생성
    public static Trade sell(Account account, Stock stock, Long quantity, Long price, Long averagePriceBeforeSell, Long sellProfitAmount, Double sellProfitRate) {
        return new Trade(account, stock, TradeType.SELL, quantity, price, averagePriceBeforeSell, sellProfitAmount, sellProfitRate);
    }


}