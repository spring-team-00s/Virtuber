package org.example.virtuber.stock.repository;

import org.example.virtuber.stock.entity.StockPriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface StockPriceHistoryRepository extends JpaRepository<StockPriceHistory, Long> {
  // 지정 기간동안 해당 종목 가격 변동 조회
  List<StockPriceHistory> findByStock_IdAndRecordedTimeBetweenOrderByRecordedTimeAsc(
      Long stockId,
      LocalDateTime start,
      LocalDateTime end
  );
}