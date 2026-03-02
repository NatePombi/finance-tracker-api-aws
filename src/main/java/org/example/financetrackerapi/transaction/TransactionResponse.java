package org.example.financetrackerapi.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.financetrackerapi.account.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TransactionResponse (
     Long id,
     BigDecimal amount,
     TransactionType type,
     String categoryName,
     String description,
     Long accountId,
     AccountType accountType,
     LocalDate date,
     LocalDateTime createdAt)
{}
