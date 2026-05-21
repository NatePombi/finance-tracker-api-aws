package org.example.financetrackerapi.transaction.dto;

import java.math.BigDecimal;


public record CategorySummaryResponse (
     String category,
     BigDecimal total){ }
