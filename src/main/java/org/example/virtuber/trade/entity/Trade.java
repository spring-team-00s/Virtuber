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

    // 거래내역 생성자
    private Trade(Account account, Stock stock, TradeType tradeType, Long quantity, Long price) {
        this.account = account;
        this.stock = stock;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = price;
        this.tradedTime = LocalDateTime.now();
    }
    // 매수 거래 생성
    public static Trade buy(Account account, Stock stock, Long quantity, Long price) {
        return new Trade(account, stock, TradeType.BUY, quantity, price);
    }
    // 매도 거래 생성
    public static Trade sell(Account account, Stock stock, Long quantity, Long price) {
        return new Trade(account, stock, TradeType.SELL, quantity, price);
    }
}