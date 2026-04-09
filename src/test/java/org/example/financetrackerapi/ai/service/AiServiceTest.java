package org.example.financetrackerapi.ai.service;

import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountType;
import org.example.financetrackerapi.ai.AiService;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryType;
import org.example.financetrackerapi.transaction.Transaction;
import org.example.financetrackerapi.transaction.TransactionRepository;
import org.example.financetrackerapi.transaction.TransactionResponse;
import org.example.financetrackerapi.transaction.TransactionType;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AiServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private AiService aiService;

    private User testUser;
    private Account testAccount;
    private Category testCategory;


    @BeforeEach
    void startUp(){
        testUser = User.create("test@gmail.com","testPass");
        testAccount = Account.create("Savings", AccountType.SAVINGS,testUser);
        testCategory = Category.createCategory("Income", CategoryType.CREDIT,testUser);
    }

    @Test
    void shouldGetAiAnalysis(){
        Transaction transaction = Transaction.createTransaction(BigDecimal.valueOf(10000), TransactionType.DEBIT, LocalDate.now(),"Car Payment",testAccount,testCategory);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(transactionRepository.findAllByAccountUser(testUser)).thenReturn(List.of(transaction));

        String prompt = aiService.getTransactionsForAiAnalysis(testUser.getEmail());


        assertThat(prompt).isNotBlank();
        assertThat(prompt).contains("Car Payment");
        assertThat(prompt).contains("10000");
    }
}
