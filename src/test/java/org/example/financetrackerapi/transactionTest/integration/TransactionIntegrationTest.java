package org.example.financetrackerapi.transactionTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountRepository;
import org.example.financetrackerapi.account.AccountType;
import org.example.financetrackerapi.ai.AiService;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryRepository;
import org.example.financetrackerapi.category.CategoryType;
import org.example.financetrackerapi.transaction.*;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
public class TransactionIntegrationTest {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private PasswordEncoder passwordEncoder;



    private User testUser;
    private Category testCategory1;
    private Category testCategory2;
    private Account testAccount;

    @BeforeEach
    void startUp() throws Exception {
        testUser = User.create("test@gmail.com",passwordEncoder.encode("test"));
        userRepository.save(testUser);

        testAccount = Account.create("Savings Account", AccountType.SAVINGS,testUser);
        accountRepository.save(testAccount);

        testCategory1 = Category.createCategory("Savings", CategoryType.CREDIT,testUser);
        testCategory2 = Category.createCategory("Groceries", CategoryType.DEBIT,testUser);
        categoryRepository.save(testCategory1);
        categoryRepository.save(testCategory2);
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldCreateTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(2000), TransactionType.CREDIT,1L,1L, LocalDate.now(),"Deposit to Savings");

        TransactionResponse response = transactionService.create(request,testUser.getEmail());

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());


        assertThat(response).isNotNull();
        assertThat(response.amount()).isEqualTo(request.getAmount());
        assertThat(response.description()).isEqualTo(request.getDescription());
        assertThat(response.date()).isEqualTo(request.getDate());

    }


    @Test
    void shouldFailCreateTransaction_NotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/v1/transactions"))
                .andExpect(status().isUnauthorized());

    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateTransaction_BadCredentials() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(2000), null,1L,1L, LocalDate.now(),"Deposit to Savings");

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateTransaction_NegativeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(-2000), null,1L,1L, LocalDate.now(),"Deposit to Savings");

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetTransactions () throws Exception {
        Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.now(),"Deposit to Savings",testAccount,testCategory1);
        Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);

        transactionRepository.save(tran1);
        transactionRepository.save(tran2);

        Pageable pageable = PageRequest.of(0,10);

        Page<TransactionResponse> responsePage = transactionService.getTransactions(testUser.getEmail(),pageable);

        mockMvc.perform(get("/api/v1/transactions")
                .with(csrf()))
                .andExpect(status().isOk());


        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(2);
        assertThat(responsePage.getTotalPages()).isEqualTo(1);

        List<TransactionResponse> list = responsePage.get().toList();

        assertThat(list.get(0).amount()).isEqualTo(tran1.getAmount());
        assertThat(list.get(0).description()).isEqualTo(tran1.getDescription());
        assertThat(list.get(0).date()).isEqualTo(tran1.getDate());

        assertThat(list.get(1).amount()).isEqualTo(tran2.getAmount());
        assertThat(list.get(1).description()).isEqualTo(tran2.getDescription());
        assertThat(list.get(1).date()).isEqualTo(tran2.getDate());


    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetTransactions_ByDate() throws Exception {
        Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.of(2026,2,15),"Deposit to Savings",testAccount,testCategory1);
        Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);
        Transaction tran3 = Transaction.createTransaction(new BigDecimal(400),TransactionType.DEBIT,LocalDate.of(2025,12,30),"From FoodLovers",testAccount,testCategory2);


        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);

        Pageable pageable = PageRequest.of(0,10);

        Page<TransactionResponse> responsePage = transactionService.getTransactionsByDate(testUser.getEmail(),LocalDate.of(2025,11,1),LocalDate.of(2026,2,20),pageable);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("from","2025-11-01")
                        .param("to","2026-02-20")
                        .with(csrf()))
                .andExpect(status().isOk());


        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(2);
        assertThat(responsePage.getTotalPages()).isEqualTo(1);

        List<TransactionResponse> list = responsePage.get().toList();

        assertThat(list.get(0).amount()).isEqualTo(tran1.getAmount());
        assertThat(list.get(0).description()).isEqualTo(tran1.getDescription());
        assertThat(list.get(0).date()).isEqualTo(tran1.getDate());

        assertThat(list.get(1).amount()).isEqualTo(tran3.getAmount());
        assertThat(list.get(1).description()).isEqualTo(tran3.getDescription());
        assertThat(list.get(1).date()).isEqualTo(tran3.getDate());


    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetTransactions_FromDate() throws Exception {
        Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.of(2026,2,15),"Deposit to Savings",testAccount,testCategory1);
        Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);
        Transaction tran3 = Transaction.createTransaction(new BigDecimal(400),TransactionType.DEBIT,LocalDate.of(2025,12,30),"From FoodLovers",testAccount,testCategory2);


        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);

        Pageable pageable = PageRequest.of(0,10);

        Page<TransactionResponse> responsePage = transactionService.getTransactionsByFromDate(testUser.getEmail(),LocalDate.of(2026,3,1),pageable);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("from","2025-03-01")
                        .with(csrf()))
                .andExpect(status().isOk());


        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getTotalPages()).isEqualTo(1);

        List<TransactionResponse> list = responsePage.get().toList();

        assertThat(list.get(0).amount()).isEqualTo(tran2.getAmount());
        assertThat(list.get(0).description()).isEqualTo(tran2.getDescription());
        assertThat(list.get(0).date()).isEqualTo(tran2.getDate());


    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetTransactions_ToDate() throws Exception {
        Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.of(2026,2,15),"Deposit to Savings",testAccount,testCategory1);
        Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);
        Transaction tran3 = Transaction.createTransaction(new BigDecimal(400),TransactionType.DEBIT,LocalDate.of(2025,12,30),"From FoodLovers",testAccount,testCategory2);


        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);

        Pageable pageable = PageRequest.of(0,10);

        Page<TransactionResponse> responsePage = transactionService.getTransactionByToDate(testUser.getEmail(),LocalDate.of(2025,12,30),pageable);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("to","2025-12-30")
                        .with(csrf()))
                .andExpect(status().isOk());


        assertThat(responsePage).isNotNull();
        assertThat(responsePage.getTotalElements()).isEqualTo(1);
        assertThat(responsePage.getTotalPages()).isEqualTo(1);

        List<TransactionResponse> list = responsePage.get().toList();

        assertThat(list.get(0).amount()).isEqualTo(tran3.getAmount());
        assertThat(list.get(0).description()).isEqualTo(tran3.getDescription());
        assertThat(list.get(0).date()).isEqualTo(tran3.getDate());


    }


    @Test
    void shouldFailGetTransactions_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/transactions")
                        .param("from","2025-03-01"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailGetTransaction_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .param("from","invalid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetMonthlyTransactions() throws Exception {
        Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.now(),"Deposit to Savings",testAccount,testCategory1);
        Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);
        Transaction tran3 = Transaction.createTransaction(new BigDecimal(400),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);

        LocalDate date = LocalDate.now();


        transactionRepository.save(tran1);
        transactionRepository.save(tran2);
        transactionRepository.save(tran3);

        MonthlySummaryReport report = transactionService.getMonthlySummaryReport("test@gmail.com",date.getYear(),date.getMonth().getValue());

        mockMvc.perform(get("/api/v1/transactions/summary")
                .param("year","%s".formatted(date.getYear()))
                .param("month","%s".formatted(date.getMonth().getValue()))
                .with(csrf()))
                .andExpect(status().isOk());

        assertThat(report).isNotNull();
        assertThat(report.totalIncome()).isEqualByComparingTo("1200.00");
        assertThat(report.totalExpense()).isEqualByComparingTo("600.00");
        assertThat(report.totalBalance()).isEqualByComparingTo("600.00");
        assertThat(report.year()).isEqualTo(date.getYear());
        assertThat(report.month()).isEqualTo(date.getMonth().getValue());

   }


   @Test
    void shouldFailGetMonthlyTransactions_NotLoggedIn() throws Exception {
       mockMvc.perform(get("/api/v1/transactions/summary")
                       .param("year","2026")
                       .param("month","3"))
               .andExpect(status().isUnauthorized());

   }


   @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailGetMonthlyTransactions_BadRequest() throws Exception {
       mockMvc.perform(get("/api/v1/transactions/summary")
                       .param("year","2026")
                       .param("month","invalid")
                       .with(csrf()))
               .andExpect(status().isBadRequest());

   }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetCategorySummary() throws Exception {
       Transaction tran1 = Transaction.createTransaction(new BigDecimal(1200),TransactionType.CREDIT,LocalDate.now(),"Deposit to Savings",testAccount,testCategory1);
       Transaction tran2 = Transaction.createTransaction(new BigDecimal(200),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);
       Transaction tran3 = Transaction.createTransaction(new BigDecimal(400),TransactionType.DEBIT,LocalDate.now(),"From FoodLovers",testAccount,testCategory2);

       LocalDate date = LocalDate.now();

       transactionRepository.save(tran1);
       transactionRepository.save(tran2);
       transactionRepository.save(tran3);

        List<CategorySummaryResponse> responseList = transactionService.getCategorySummaryResponse("test@gmail.com",date.getYear(),date.getMonth().getValue());

        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year","%s".formatted(date.getYear()))
                .param("month","%s".formatted(date.getMonth().getValue()))
                .with(csrf()))
                .andExpect(status().isOk());

        assertThat(responseList).isNotNull();
        assertThat(responseList.size()).isEqualTo(1);
        assertThat(responseList.get(0).total()).isEqualByComparingTo("600.00");
        assertThat(responseList.get(0).category()).isEqualTo("Groceries");

   }

   @Test
    void shouldFailGetCategorySummary_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year","2026")
                .param("month","3")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
   }

   @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailGetCategorySummary_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year","2026")
                .param("month","invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());
   }



}
