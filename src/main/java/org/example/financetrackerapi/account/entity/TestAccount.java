package org.example.financetrackerapi.account.entity;

import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.user.entity.User;

public class TestAccount extends Account{
   public TestAccount(Long id, String name, AccountType accountType, User user) {
        super(id, name, accountType, user);
    }
}
