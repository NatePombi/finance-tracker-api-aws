package org.example.financetrackerapi.account.enums;

import org.example.financetrackerapi.exception.AccountTypeMismatchException;

public enum AccountType {
    CREDIT,CHECKING,SAVINGS;

    public static AccountType checkType(String type){
        try{
            return AccountType.valueOf(type.toUpperCase().trim());
        }
        catch (IllegalArgumentException e){
            throw new AccountTypeMismatchException("Invalid account type");
        }
    }
}
