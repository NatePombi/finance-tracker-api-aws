package org.example.financetrackerapi.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.example.financetrackerapi.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
@Tag(name = "Account Controller", description = "Operation Related to Account Management")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @Operation(summary = "Create a new Accounts",
            description = "Creates an Account for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest accountRequest, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(accountRequest, email));
    }

    @Operation(summary = "Gets All Account",
            description = "Gets all Accounts for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Account Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll(@Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(accountService.getAccounts(email));
    }

    @Operation(summary = "Get Account by id",
            description = "Get Account by id for authenticated User")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Get Account Successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Not Found"),
            @ApiResponse(responseCode = "409", description = "Conflict"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BalanceResponse> getBalance(@Parameter(description = "Account ID") @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal(expression = "username") String email){
        return ResponseEntity.ok(accountService.getBalance(email,id));
    }

}
