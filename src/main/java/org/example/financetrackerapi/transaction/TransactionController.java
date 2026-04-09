package org.example.financetrackerapi.transaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.ai.AiResponse;
import org.example.financetrackerapi.ai.AiService;
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
@Tag(name = "Transactions", description = "Operations related to Financial transactions")
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "Create a new Transactions",
                description = "Creates a financial transactions for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody @Valid TransactionRequest transactionRequest, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(transactionRequest, email));
    }

    @Operation(summary = "Get Transactions",
            description = "Gets All transactions from authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Gets Transactions successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
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

    @Operation(summary = "Gets monthly Summary Report",
            description = "Gets monthly summary Report from authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Monthly Summary Report Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryReport> getMonthlySummary(@Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email,
                                                                  @RequestParam int year,
                                                                  @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getMonthlySummaryReport(email, year, month));
    }

    @Operation(summary = "Get Category Summary Response",
            description = "Get Category for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Category Summary Response Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping("/summary/category")
    public ResponseEntity<List<CategorySummaryResponse>> getCategorySummary(@Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email,
                                                                      @RequestParam int year,
                                                                      @Min(1) @Max(12) @RequestParam int month) {
        return ResponseEntity.ok(transactionService.getCategorySummaryResponse(email,year,month));
    }



}
