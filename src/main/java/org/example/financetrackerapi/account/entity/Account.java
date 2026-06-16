package org.example.financetrackerapi.account.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    public static Account create(String name,AccountType accountType, User user) {

        if(name == null || name.isBlank()){
            throw new IllegalArgumentException("Name cannot be null or empty");
        }

        Account account = new Account();
        account.name = name;
        account.accountType = accountType;
        account.user = user;
        return account;
    }

   Account (Long id ,String name,AccountType accountType, User user) {
        this.id = id;
        this.name = name;
        this.accountType = accountType;
        this.user = user;

    }




}
