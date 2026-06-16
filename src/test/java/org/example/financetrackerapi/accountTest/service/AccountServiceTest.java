package org.example.financetrackerapi.accountTest.service;

import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.dto.BalanceResponse;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.account.entity.TestAccount;
import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.account.repository.AccountRepository;
import org.example.financetrackerapi.account.service.AccountService;
import org.example.financetrackerapi.exception.AccountNotFoundException;
import org.example.financetrackerapi.exception.AccountTypeMismatchException;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.transaction.repository.TransactionRepository;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
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
    private Account testAccount;

    @BeforeEach
    void startUp(){
        testUser = User.create("test@gmail.com","testPass");
        testAccount = new TestAccount(1L,"Savings Account",AccountType.SAVINGS,testUser);
    }

    @Test
    void shouldCreateAccount() {
        AccountRequest request = new AccountRequest("Savings Account", AccountType.SAVINGS);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = service.create(request,testUser.getEmail());
        assertThat(response).isNotNull();
        verify(accountRepository).save(any(Account.class));

        assertThat(response.accountType()).isEqualTo(request.getAccountType());
    }

    @Test
    void shouldFailCreateAccount_UserNotFound() {
        AccountRequest request = new AccountRequest("Savings Account", AccountType.SAVINGS);

        when(userRepository.findByEmail(testUser.getEmail())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class,()->{
            service.create(request,"test@gmail.com");
        });

        verify(accountRepository, never()).save(any(Account.class));
    }



   @Test
    void shouldGetAllUsersAccounts(){

        Account acc2 = new TestAccount(2L,"Credit Account",AccountType.CREDIT,testUser);

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        when(accountRepository.findByUserEmail(testUser.getEmail())).thenReturn(List.of(testAccount,acc2));
        List<AccountResponse> responses = service.getAccounts(testUser.getEmail());

        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(2);

        assertThat(responses.get(0).id()).isEqualTo(testAccount.getId());
        assertThat(responses.get(0).name()).isEqualTo(testAccount.getName());
        assertThat(responses.get(0).accountType()).isEqualTo(testAccount.getAccountType());

        assertThat(responses.get(1).id()).isEqualTo(acc2.getId());
        assertThat(responses.get(1).name()).isEqualTo(acc2.getName());
        assertThat(responses.get(1).accountType()).isEqualTo(acc2.getAccountType());

   }


   @Test
    void shouldGetBalance(){
        when(accountRepository.findByIdAndUserEmail(1L,testUser.getEmail())).thenReturn(Optional.ofNullable(mock(Account.class)));
        when(transactionRepository.balance(any(Account.class))).thenReturn(BigDecimal.valueOf(400));

        BalanceResponse balance = service.getBalance(testUser.getEmail(),1L);

        assertThat(balance.balance()).isEqualTo(BigDecimal.valueOf(400));
   }

   @Test
    void shouldFailGetBalance_AccountNotFound(){

        assertThrows(AccountNotFoundException.class,()->{
            service.getBalance("test@yahoo.com",1L);
        });

        verify(transactionRepository,never()).balance(any(Account.class));
   }
}
