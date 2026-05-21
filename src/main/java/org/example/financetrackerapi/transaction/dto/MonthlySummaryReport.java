package org.example.financetrackerapi.transaction.dto;

import java.math.BigDecimal;


public record MonthlySummaryReport (
    int year,
    int month,
    BigDecimal totalIncome,
    BigDecimal totalExpense,
    BigDecimal totalBalance)
{}
