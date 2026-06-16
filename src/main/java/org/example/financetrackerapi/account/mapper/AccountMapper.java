package org.example.financetrackerapi.account.mapper;

import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.user.entity.User;

public class AccountMapper {

    public static AccountResponse toAccountResponse(Account account, User user) {

        if (account == null) {return null;}

        return new AccountResponse(
                account.getId(),
                account.getName(),
                account.getAccountType(),
                user.getId()
        );
    }
}
