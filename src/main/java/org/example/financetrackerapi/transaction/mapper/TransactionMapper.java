package org.example.financetrackerapi.transaction.mapper;

import org.example.financetrackerapi.transaction.dto.TransactionResponse;
import org.example.financetrackerapi.transaction.entity.Transaction;

public class TransactionMapper {
    public static TransactionResponse toTransactionResponse(Transaction transaction) {
        return new TransactionResponse(transaction.getId(),transaction.getAmount(),transaction.getType(),transaction.getCategory().getName(),transaction.getDescription(),transaction.getAccount().getId(),transaction.getAccount().getAccountType(), transaction.getDate(),transaction.getCreatedAt());
    }
}
