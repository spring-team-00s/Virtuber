package org.example.virtuber.stock.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "stock_price_histories",
        indexes = {
                @Index(
                        name = "idx_stock_price_histories_stock_time",
                        columnList = "stock_id, recorded_time"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // stock_price_histories.stock_id -> stocks.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Column(nullable = false)
    private Long price;

    @Column(name = "recorded_time", nullable = false)
    private LocalDateTime recordedTime;
}