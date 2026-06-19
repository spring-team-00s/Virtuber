package org.example.virtuber.account;

import jakarta.persistence.LockModeType;
import org.example.virtuber.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
  // UserID로 어카운트 찾기
  Optional<Account> findByUser_Id(Long userId);
  // 동시 수정 방지
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM Account a WHERE a.user.id = :userId")
  Optional<Account> findByUserIdForUpdate(@Param("userId") Long userId);

}
