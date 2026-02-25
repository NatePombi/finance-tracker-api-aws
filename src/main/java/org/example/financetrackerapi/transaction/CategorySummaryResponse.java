package org.example.financetrackerapi.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;


public record CategorySummaryResponse (
     String category,
     BigDecimal total){ }
