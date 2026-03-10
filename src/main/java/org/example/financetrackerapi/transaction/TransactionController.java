package org.example.financetrackerapi.transaction;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;


    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest transactionRequest, @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(transactionRequest, email));
    }


    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@Parameter(hidden = true)
                                                                     @AuthenticationPrincipal(expression = "username") String email,
                                                                     @RequestParam(required = false)LocalDate from,
                                                                     @RequestParam(required = false) LocalDate to,
                                                                     Pageable pageable) {
        if(from!= null && to!=null) {
            if(from.isAfter(to)){
                throw new IllegalArgumentException("From date must be before TO date");
            }

            return ResponseEntity.ok(transactionService.getTransactionsByDate(email, from, to,pageable));
        }

        if(from != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByFromDate(email,from,pageable));
        }

        if(to != null) {
            return ResponseEntity.ok(transactionService.getTransactionByToDate(email,to,pageable));
        }

        return ResponseEntity.ok(transactionService.getTransactions(email,pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryReport> getMonthlySummary(@AuthenticationPrincipal(expression = "username") String email,
                                                                  @RequestParam int year,
                                                                  @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getMonthlySummaryReport(email, year, month));
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(@AuthenticationPrincipal(expression = "username") String email,
                                                                      @RequestParam int year,
                                                                      @Min(1) @Max(12) @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getCategorySummaryResponse(email,year,month));
    }

}
