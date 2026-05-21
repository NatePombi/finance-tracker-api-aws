package org.example.financetrackerapi.account.dto;


import org.example.financetrackerapi.account.enums.AccountType;

public record AccountResponse(
    Long id,
    String name,
    AccountType accountType,
    Long userId){
}
