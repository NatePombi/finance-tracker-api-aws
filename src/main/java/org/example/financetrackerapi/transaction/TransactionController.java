package org.example.financetrackerapi.transaction;

import jakarta.validation.Valid;
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
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest transactionRequest, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(transactionRequest, user));
    }


    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(@AuthenticationPrincipal User user,
                                                                     @RequestParam(required = false)LocalDate from,
                                                                     @RequestParam(required = false) LocalDate to,
                                                                     Pageable pageable) {
        if(from!= null && to!=null) {
            if(from.isAfter(to)){
                throw new IllegalArgumentException("From date must be before TO date");
            }

            return ResponseEntity.ok(transactionService.getTransactionsByDate(user, from, to,pageable));
        }

        if(from != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByFromDate(user,from,pageable));
        }

        if(to != null) {
            return ResponseEntity.ok(transactionService.getTransactionByToDate(user,to,pageable));
        }

        return ResponseEntity.ok(transactionService.getTransactions(user,pageable));
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryReport> getMonthlySummary(@AuthenticationPrincipal User user,
                                                                  @RequestParam int year,
                                                                  @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getMonthlySummaryReport(user, year, month));
    }

    @GetMapping("/summary/category")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(@AuthenticationPrincipal User user,
                                                                      @RequestParam int year,
                                                                      @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getCategorySummaryResponse(user,year,month));
    }

}
