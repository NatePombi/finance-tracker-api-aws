package org.example.financetrackerapi.account;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountRequest {
    @NotBlank(message = "account name cannot be empty")
    private String accountName;
    @NotNull(message = "account type cannot be empty")
    private AccountType accountType;
}
