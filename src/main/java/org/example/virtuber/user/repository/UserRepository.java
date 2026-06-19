package org.example.virtuber.user.repository;

import org.example.virtuber.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  // userid 중복 방지
  boolean existsByUserId(String userId);
  // 로그인 시 userid 탐색
  Optional<User> findByUserId(String userId);
}
