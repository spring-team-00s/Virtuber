package org.example.virtuber.trade.repository;

import org.example.virtuber.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
  // 계좌 거래내역 최신순 정렬
  List<Trade> findAllByAccount_IdOrderByTradedTimeDesc(Long accountId);
  // 한강버튼 거래내역 삭제
  void deleteAllByAccount_Id(Long accountId);
}