package org.example.virtuber.account.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.virtuber.user.entity.User;

@Entity
@Table(name = "accounts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // accounts.user_id -> users.id
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "cash_balance", nullable = false)
    private Long cashBalance;

    @Column(name = "seed_money", nullable = false)
    private Long seedMoney;

    public Account(User user, Long seedMoney) {
        this.user = user;
        this.cashBalance = seedMoney;
        this.seedMoney = seedMoney;
    }
}