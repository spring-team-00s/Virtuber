package org.example.virtuber.stock.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "stocks",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_stocks_stock_code", columnNames = "stock_code"),
                @UniqueConstraint(name = "uk_stocks_stock_name", columnNames = "stock_name")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stock_code", nullable = false, unique = true, length = 20)
    private String stockCode;

    @Column(name = "stock_name", nullable = false, unique = true, length = 100)
    private String stockName;

    @Column(name = "current_price", nullable = false)
    private Long currentPrice;

    @Column(name = "up_price", nullable = false)
    private Long upPrice;

    @Column(name = "low_price", nullable = false)
    private Long lowPrice;

    @Column(name = "company_info", columnDefinition = "TEXT")
    private String companyInfo;

    @Column(name = "financial_info", columnDefinition = "TEXT")
    private String financialInfo;
}