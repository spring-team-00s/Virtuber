package org.example.virtuber.stock.repository;

import org.example.virtuber.stock.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
  // 종목 코드로 주식 찾기
  Optional<Stock> findByStockCode(String stockCode);
  // 주식 코드 중복 입력 방지
  boolean existsByStockCode(String stockCode);
}
