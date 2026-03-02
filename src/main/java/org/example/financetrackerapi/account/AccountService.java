package org.example.financetrackerapi.account;

import lombok.AllArgsConstructor;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.transaction.TransactionRepository;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
@Service
@AllArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public AccountResponse create(AccountRequest request, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(()-> new UserNotFoundException("User not found"));

        Account account = Account.create(request.getAccountName(),request.getAccountType(),user);

        accountRepository.save(account);

        return new AccountResponse(account.getId(), account.getName(), account.getAccountType(),user.getId());

    }

    public List<AccountResponse> getAccounts(String email) {
        return accountRepository.findByUserEmail(email).stream()
                .map(acc -> new AccountResponse(acc.getId(),acc.getName(), acc.getAccountType(), acc.getUser().getId()))
                .toList();
    }

    public BalanceResponse getBalance(String email, Long accId) {
        Account acc = accountRepository.findByIdAndUserEmail(accId,email).orElseThrow(()-> new IllegalArgumentException("Account not found"));
        BigDecimal balance = transactionRepository.balance(acc);
        return new BalanceResponse(balance);
    }
}
