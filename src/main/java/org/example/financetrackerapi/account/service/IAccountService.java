package org.example.financetrackerapi.account.service;

import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.dto.BalanceResponse;

import java.util.List;

public interface IAccountService {
    AccountResponse create(AccountRequest accountRequest, String email);
    List<AccountResponse> getAccounts(String email);
    BalanceResponse getBalance(String email, Long accId);
}
