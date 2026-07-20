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
import org.example.financetrackerapi.exception.AccountsNotFoundException;
import org.example.financetrackerapi.exception.UserNotFoundException;
import org.example.financetrackerapi.transaction.repository.TransactionRepository;
import org.example.financetrackerapi.user.entity.TestUser;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

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
        testUser = new TestUser(12L,"test@gmail.com","hashed-password");
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
       Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
        Account acc2 = new TestAccount(2L,"Credit Account",AccountType.CREDIT,testUser);

        Page<Account> page = new PageImpl<>(List.of(testAccount,acc2));

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));

        when(accountRepository.findAllByUserId(testUser.getId(),pageable)).thenReturn(page);
        Page<AccountResponse> responses = service.getAccounts(testUser.getEmail(),0,5,"id","desc");

        assertThat(responses).isNotNull();
        assertThat(responses.getSize()).isEqualTo(2);
        assertThat(responses.getTotalElements()).isEqualTo(2);
        assertThat(responses.hasNext()).isFalse();

   }

   @Test
    void shouldFailGetAllAccounts_NoAccountsPresent() {
        Pageable pageable = PageRequest.of(0,5,Sort.by("id").descending());

        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(accountRepository.findAllByUserId(testUser.getId(),pageable)).thenReturn(null);

        assertThrows(AccountsNotFoundException.class,()->{
            service.getAccounts("test@gmail.com",0,5,"id","desc");
        });

   }

   @Test
   void shouldFailGetAllAccounts_UserNotFound() {

        assertThrows(UserNotFoundException.class,()->{
            service.getAccounts("test@gmail.com",0,5,"id","desc");
        });

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
