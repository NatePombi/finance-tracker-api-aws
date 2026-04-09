package org.example.financetrackerapi.transactionTest.service;

import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountRepository;
import org.example.financetrackerapi.account.AccountType;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryRepository;
import org.example.financetrackerapi.category.CategoryType;
import org.example.financetrackerapi.exception.AccountNotFoundException;
import org.example.financetrackerapi.exception.CategoryNotFoundException;
import org.example.financetrackerapi.transaction.*;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private TransactionService transactionService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;

    @BeforeEach
    void startup(){
        testUser = User.create("test@gmail.com","testPass");
        testAccount = Account.create("Savings Account", AccountType.SAVINGS,testUser);
        testCategory = Category.createCategory("Savings", CategoryType.CREDIT,testUser);
    }


    @Test
    void shouldCreateTransaction(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(testCategory));
        when(accountRepository.findByIdAndUserEmail(eq(1L),eq("test@gmail.com"))).thenReturn(Optional.of(testAccount));

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(200), TransactionType.CREDIT,1L,1L,LocalDate.now(),"Depositing into Savings Account");

        TransactionResponse response = transactionService.create(request,testUser.getEmail());

        verify(transactionRepository,atLeast(1)).save(any(Transaction.class));

        assertThat(response.categoryName()).isEqualTo("Savings");
        assertThat(response.accountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(200));
    }

    @Test
    void shouldFailCreateTransaction_AccountNotFound(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findByIdAndUser(1L,testUser)).thenReturn(Optional.of(testCategory));

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(200), TransactionType.CREDIT,1L,1L,LocalDate.now(),"Depositing into Savings Account");

        assertThrows(AccountNotFoundException.class,()->{
            transactionService.create(request,testUser.getEmail());
        });
    }

    @Test
    void shouldFailCreateTransaction_CategoryNotFound(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        TransactionRequest request = new TransactionRequest(BigDecimal.valueOf(200), TransactionType.CREDIT,1L,1L,LocalDate.now(),"Depositing into Savings Account");

        assertThrows(CategoryNotFoundException.class,()-> {
            transactionService.create(request, testUser.getEmail());
        });
    }


    @Test
    void shouldFailCreateTransaction_TypeMismatch(){

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        Pageable pageable = PageRequest.of(0,10);
        Transaction tran1 = Transaction.createTransaction(BigDecimal.valueOf(200),TransactionType.CREDIT,LocalDate.now(),"Deposit into Saving",testAccount,testCategory);
        Transaction tran2 = Transaction.createTransaction(BigDecimal.valueOf(500),TransactionType.DEBIT,LocalDate.now(),"Groceries",testAccount,testCategory);
        Transaction tran3 = Transaction.createTransaction(BigDecimal.valueOf(600),TransactionType.CREDIT,LocalDate.now(),"Salary",testAccount,testCategory);

        List<Transaction> transactions = List.of(tran1,tran2,tran3);
        Page<Transaction> transactionPage = new PageImpl<>(transactions,pageable,1);

        when(transactionRepository.findAllByAccountUserWithCategory(testUser,pageable)).thenReturn(transactionPage);

        Page<TransactionResponse> responses = transactionService.getTransactions("test@gmail.com",pageable);

        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(3);
        assertThat(responses.getTotalPages()).isEqualTo(1);

        verify(transactionRepository).findAllByAccountUserWithCategory(testUser,pageable);

    }


    @Test
    void shouldGetTransactionsByDate(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        Pageable pageable = PageRequest.of(0,10);

        Transaction tran1 = Transaction.createTransaction(BigDecimal.valueOf(200),TransactionType.CREDIT,LocalDate.now(),"Deposit into Saving",testAccount,testCategory);
        Transaction tran2 = Transaction.createTransaction(BigDecimal.valueOf(500),TransactionType.DEBIT,LocalDate.now(),"Groceries",testAccount,testCategory);
        Transaction tran3 = Transaction.createTransaction(BigDecimal.valueOf(600),TransactionType.CREDIT,LocalDate.now(),"Salary",testAccount,testCategory);

        List<Transaction> transactions = List.of(tran1,tran2,tran3);

        Page<Transaction> page = new PageImpl<>(transactions,pageable,1);

        LocalDate to = LocalDate.now();
        LocalDate from = LocalDate.parse("2025-07-12");

        when(transactionRepository.findAllByAccountUserWithCategoryAndDateBetween(testUser,from,to,pageable)).thenReturn(page);

        Page<TransactionResponse> responses = transactionService.getTransactionsByDate("test@gmail.com",from,to,pageable);

        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(3);
        assertThat(responses.getTotalPages()).isEqualTo(1);

        verify(transactionRepository).findAllByAccountUserWithCategoryAndDateBetween(testUser,from,to,pageable);

    }

    @Test
    void shouldFailGetTransactionsByDate_FromDateMustBeBeforeToDate(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        Pageable pageable = PageRequest.of(0,10);

        LocalDate from = LocalDate.parse("2026-07-07");

        assertThrows(IllegalArgumentException.class,()->{
            transactionService.getTransactionsByDate(testUser.getEmail(),from,LocalDate.now(),pageable);
        });


    }


    @Test
    void shouldGetTransactionByFromDate(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        Pageable pageable = PageRequest.of(0,10);
        Transaction tran1 = Transaction.createTransaction(BigDecimal.valueOf(200),TransactionType.CREDIT,LocalDate.now(),"Deposit into Saving",testAccount,testCategory);
        Transaction tran2 = Transaction.createTransaction(BigDecimal.valueOf(500),TransactionType.DEBIT,LocalDate.now(),"Groceries",testAccount,testCategory);
        Transaction tran3 = Transaction.createTransaction(BigDecimal.valueOf(600),TransactionType.CREDIT,LocalDate.now(),"Salary",testAccount,testCategory);

        List<Transaction> transactions = List.of(tran1,tran2,tran3);
        Page<Transaction> transactionPage = new PageImpl<>(transactions,pageable,1);

        when(transactionRepository.findAllByAccountUserFromDate(testUser,LocalDate.now(),pageable)).thenReturn(transactionPage);

        Page<TransactionResponse> responses = transactionService.getTransactionsByFromDate(testUser.getEmail(),LocalDate.now(),pageable);

        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(3);
        assertThat(responses.getTotalPages()).isEqualTo(1);

        verify(transactionRepository).findAllByAccountUserFromDate(testUser,LocalDate.now(),pageable);


    }

    @Test
    void shouldGetTransactionByToDate(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));

        Pageable pageable = PageRequest.of(0,10);
        Transaction tran1 = Transaction.createTransaction(BigDecimal.valueOf(200),TransactionType.CREDIT,LocalDate.now(),"Deposit into Saving",testAccount,testCategory);
        Transaction tran2 = Transaction.createTransaction(BigDecimal.valueOf(500),TransactionType.DEBIT,LocalDate.now(),"Groceries",testAccount,testCategory);
        Transaction tran3 = Transaction.createTransaction(BigDecimal.valueOf(600),TransactionType.CREDIT,LocalDate.now(),"Salary",testAccount,testCategory);

        List<Transaction> transactions = List.of(tran1,tran2,tran3);
        Page<Transaction> transactionPage = new PageImpl<>(transactions,pageable,1);

        when(transactionRepository.findAllByAccountUserToDate(testUser,LocalDate.now(),pageable)).thenReturn(transactionPage);

        Page<TransactionResponse> responses = transactionService.getTransactionByToDate(testUser.getEmail(),LocalDate.now(),pageable);

        assertThat(responses).isNotNull();
        assertThat(responses.getTotalElements()).isEqualTo(3);
        assertThat(responses.getTotalPages()).isEqualTo(1);

        verify(transactionRepository).findAllByAccountUserToDate(testUser,LocalDate.now(),pageable);


    }


    @Test
    void shouldGetMonthlySummaryReport(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        when(transactionRepository.sumCreditByMonth(testUser,2026,3)).thenReturn(new BigDecimal(5000));
        when(transactionRepository.sumDebitByMonth(testUser,2026,3)).thenReturn(new BigDecimal(2000));

        MonthlySummaryReport report = transactionService.getMonthlySummaryReport(testUser.getEmail(),2026,3);

        assertThat(report).isNotNull();
        assertThat(report.month()).isEqualTo(3);
        assertThat(report.year()).isEqualTo(2026);
        assertThat(report.totalBalance()).isEqualTo(new BigDecimal(3000));
        assertThat(report.totalExpense()).isEqualTo(new BigDecimal(2000));
        assertThat(report.totalIncome()).isEqualTo(new BigDecimal(5000));
    }

    @Test
    void shouldGetCategorySummaryResponse(){
        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(testUser));
        CategorySummaryResponse sumRes1 = new CategorySummaryResponse("Savings",new BigDecimal(6000));
        CategorySummaryResponse sumRes2 = new CategorySummaryResponse("Loan",new BigDecimal(16000));
        CategorySummaryResponse sumRes3 = new CategorySummaryResponse("Groceries",new BigDecimal(1000));

        List<CategorySummaryResponse> responseList = List.of(sumRes1,sumRes2,sumRes3);

        when(transactionRepository.sumDebitByCategoryForMonth(testUser,2026,3)).thenReturn(responseList);

        List<CategorySummaryResponse> responses = transactionService.getCategorySummaryResponse(testUser.getEmail(),2026,3);

        assertThat(responses).isNotNull();
        assertThat(responses.size()).isEqualTo(3);
        assertThat(responses.get(0)).isEqualTo(sumRes1);
        assertThat(responses.get(1)).isEqualTo(sumRes2);
        assertThat(responses.get(2)).isEqualTo(sumRes3);
    }







}
