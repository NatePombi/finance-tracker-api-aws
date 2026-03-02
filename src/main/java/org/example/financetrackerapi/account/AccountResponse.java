package org.example.financetrackerapi.account;


public record AccountResponse(
    Long id,
    String name,
    AccountType accountType,
    Long userId){
}
