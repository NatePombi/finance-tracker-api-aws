package org.example.financetrackerapi.transaction;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    @Positive
    @NotNull(message = "amount cannot be empty")
    @Digits(integer = 12, fraction = 2)
    private BigDecimal amount;
    @NotNull(message = "type cannot be empty")
    private TransactionType type;
    @NotNull(message = "category id cannot be empty")
    private Long categoryId;
    @NotNull(message = "date cannot be empty")
    private LocalDate date;
    @Size(max = 255)
    private String description;

}
