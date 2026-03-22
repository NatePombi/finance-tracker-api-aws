package org.example.financetrackerapi.account;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Schema(description = "Account Request")
@Getter
@AllArgsConstructor
public class AccountRequest {
    @Schema(description = "Name of the account you wish to create", example = "Second Account")
    @NotBlank(message = "account name cannot be empty")
    private String accountName;
    @Schema(description = "Type of account you wish to create" , example = "Credit")
    @NotNull(message = "account type cannot be empty")
    private AccountType accountType;
}
