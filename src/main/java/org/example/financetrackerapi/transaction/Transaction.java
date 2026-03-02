package org.example.financetrackerapi.transaction;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.user.User;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    @Column(length = 255)
    private String description;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "account_id",nullable = false)
    private Account account;
    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "category_id",nullable = false)
    private Category category;
    @Column(nullable = false)
    private LocalDate date;
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static Transaction createTransaction(BigDecimal transactionAmount, TransactionType type,LocalDate date,String description,Account account, Category category) {
        Transaction transaction = new Transaction();
        transaction.amount = transactionAmount;
        transaction.type = type;
        transaction.account = account;
        transaction.category = category;
        transaction.date = date;
        transaction.description = description;
        return transaction;
    }

}
