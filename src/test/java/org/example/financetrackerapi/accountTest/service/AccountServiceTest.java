package org.example.financetrackerapi.accountTest.service;

import org.example.financetrackerapi.account.*;
import org.example.financetrackerapi.transaction.TransactionRepository;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private AccountService service;

    private User testUser;

    @BeforeEach
    void startUp(){
        testUser = User.create("test@gmail.com","testPass");
    }

    @Test
    void shouldCreateAccount() {
        AccountRequest request = new AccountRequest("Savings Account", AccountType.SAVINGS);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        AccountResponse response = service.create(request,testUser.getEmail());
        assertThat(response).isNotNull();
        verify(accountRepository).save(any(Account.class));

        assertThat(response.accountType()).isEqualTo(request.getAccountType());
    }

   @Test
    void shouldGetAllUsersAccounts(){
        Account acc1 = mock(Account.class);
        Account acc2 = mock(Account.class);
        when(acc1.getId()).thenReturn(1L);
        when(acc2.getId()).thenReturn(2L);
        when(acc1.getUser()).thenReturn(testUser);
        when(acc2.getUser()).thenReturn(testUser);

        when(accountRepository.findByUserEmail(testUser.getEmail())).thenReturn(List.of(acc1,acc2));
        List<AccountResponse> responses = service.getAccounts(testUser.getEmail());

        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(2);
   }


   @Test
    void shouldGetBalance(){
        when(accountRepository.findByIdAndUserEmail(1L,testUser.getEmail())).thenReturn(Optional.ofNullable(mock(Account.class)));
        when(transactionRepository.balance(any(Account.class))).thenReturn(BigDecimal.valueOf(400));

        BalanceResponse balance = service.getBalance(testUser.getEmail(),1L);

        assertThat(balance.balance()).isEqualTo(BigDecimal.valueOf(400));
   }
}
