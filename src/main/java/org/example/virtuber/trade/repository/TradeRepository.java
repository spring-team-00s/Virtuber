package org.example.virtuber.trade.repository;

import org.example.virtuber.trade.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
  // 계좌 거래내역 최신순 정렬
  List<Trade> findAllByAccount_IdOrderByTradedTimeDesc(Long accountId);

  @Query("""
            select t
            from Trade t
            join fetch t.stock
            where t.account.id = :accountId
            order by t.tradedTime desc
            """)
  List<Trade> findTradeHistoriesByAccountId(@Param("accountId") Long accountId);
  // 한강버튼 거래내역 삭제
  void deleteAllByAccount_Id(Long accountId);
}