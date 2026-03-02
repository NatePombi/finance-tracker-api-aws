package org.example.financetrackerapi.account;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.financetrackerapi.user.User;

import java.security.SecureRandom;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public static Account create(String name,AccountType accountType, User user) {
        Account account = new Account();
        account.name = name;
        account.accountType = accountType;
        account.user = user;
        return account;
    }


}
