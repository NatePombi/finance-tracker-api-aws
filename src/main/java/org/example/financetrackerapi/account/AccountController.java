package org.example.financetrackerapi.account;

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

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest accountRequest, @AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.create(accountRequest, email));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAll(@AuthenticationPrincipal(expression = "username") String email) {
        return ResponseEntity.ok(accountService.getAccounts(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable Long id,@AuthenticationPrincipal(expression = "username") String email){
        return ResponseEntity.ok(accountService.getBalance(email,id));
    }

}
