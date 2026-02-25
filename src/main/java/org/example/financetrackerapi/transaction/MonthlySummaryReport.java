package org.example.financetrackerapi.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


public record MonthlySummaryReport (
    int year,
    int month,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal totalBalance)
{}
