package org.example.virtuber.trade.repository;

import org.example.virtuber.trade.entity.Holding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
  // 계좌가 갖고있는 특정 주식 보유 여부 확인
  Optional<Holding> findByAccount_IdAndStock_Id(Long accountId, Long stockId);
  // 계좌가 갖고있는 모든 주식 확인
  List<Holding> findAllByAccount_Id(Long accountId);
  // 한강버튼 보유주식 삭제
  void deleteAllByAccount_Id(Long accountId);

  @Query("""
      select h
      from Holding h
      join fetch h.stock
      where h.account.id = :accountId
      """)
  List<Holding> findAllByAccountIdWithStock(@Param("accountId") Long accountId);
}