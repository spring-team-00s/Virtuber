package org.example.virtuber.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_user_id", columnNames = "user_id")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(name = "user_id", nullable = false, unique = true, length = 50)
    private String userId;

    // 암호화된 비밀번호
    @Column(nullable = false)
    private String password;

    public User(String userId, String encodedPassword) {
        this.userId = userId;
        this.password = encodedPassword;
    }
}