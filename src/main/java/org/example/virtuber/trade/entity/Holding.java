package org.example.virtuber.trade.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.virtuber.account.entity.Account;
import org.example.virtuber.common.exception.BusinessException;
import org.example.virtuber.common.exception.ErrorCode;
import org.example.virtuber.stock.entity.Stock;

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_holdings_account_stock",
                        columnNames = {"account_id", "stock_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // holdings.account_id -> accounts.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    // holdings.stock_id -> stocks.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long quantity;

    @Column(name = "average_price", nullable = false)
    private Long averagePrice;

    // 첫 매수시 Holding 만들기
    public Holding(Account account, Stock stock, Long quantity, Long averagePrice) {
        this.account = account;
        this.stock = stock;
        this.quantity = quantity;
        this.averagePrice = averagePrice;
    }
    // 보유주식 추가주문시 평단가 재계산
    public void buy(Long buyQuantity, Long buyPrice) {
        Long oldTotalAmount = this.averagePrice * this.quantity;
        Long newBuyAmount = buyQuantity * buyPrice;

        Long newQuantity = this.quantity + buyQuantity;
        Long newAveragePrice = (oldTotalAmount + newBuyAmount) / newQuantity;

        this.quantity = newQuantity;
        this.averagePrice = newAveragePrice;
    }
    // 매도시 보유 수량 차감
    public void sell(Long sellQuantity) {
        if(this.quantity < sellQuantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_HOLDING);
        }
        this.quantity -= sellQuantity;
    }
    // 수량 0 되면 보유목록에서 지움
    public boolean isEmpty() {
        return this.quantity == 0L;
    }
}