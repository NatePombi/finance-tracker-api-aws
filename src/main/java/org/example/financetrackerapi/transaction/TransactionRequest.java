package org.example.financetrackerapi.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
@Schema(description = "Transaction Request")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @Positive
    @Schema(description = "The amount that was transferred")
    @NotNull(message = "amount cannot be empty")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    @Schema(description = "Type of transaction being done", example = "DEBIT")
    @NotNull(message = "type cannot be empty")
    private TransactionType type;
    @Schema(description = "id of the account that's doing the transaction")
    @NotNull(message = "account id cannot be empty")
    private Long accountId;
    @Schema(description = "id of category that's being used in the transactions")
    @NotNull(message = "category id cannot be empty")
    private Long categoryId;
    @Schema(description = "Date of the transaction")
    @NotNull(message = "date cannot be empty")
    private LocalDate date;
    @Schema(description = "Description of the transaction")
    @Size(max = 255)
    private String description;

}
