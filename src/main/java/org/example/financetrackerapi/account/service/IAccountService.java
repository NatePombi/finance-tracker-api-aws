package org.example.financetrackerapi.account.service;

import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.dto.BalanceResponse;
import org.springframework.data.domain.Page;

public interface IAccountService {
    AccountResponse create(AccountRequest accountRequest, String email);
    Page<AccountResponse> getAccounts(String email,int page,int size, String sortBy, String direction);
    BalanceResponse getBalance(String email, Long accId);
}
