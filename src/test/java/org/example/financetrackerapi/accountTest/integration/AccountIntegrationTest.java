package org.example.financetrackerapi.accountTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.account.repository.AccountRepository;
import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.category.entity.Category;
import org.example.financetrackerapi.category.repository.CategoryRepository;
import org.example.financetrackerapi.category.enums.CategoryType;
import org.example.financetrackerapi.transaction.entity.Transaction;
import org.example.financetrackerapi.transaction.repository.TransactionRepository;
import org.example.financetrackerapi.transaction.enums.TransactionType;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AccountIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private PasswordEncoder encoder;

    private User testUser;
    private Account testAccount1;
    private Account testAccount2;

    @BeforeEach
    void startUp(){
        testUser = User.create("test@gmail.com",encoder.encode("test"));
        userRepository.save(testUser);

        testAccount1 = Account.create("Savings Account", AccountType.SAVINGS, testUser);
        testAccount2 = Account.create("Credit Account", AccountType.CREDIT, testUser);
        accountRepository.save(testAccount1);
        accountRepository.save(testAccount2);

    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldCreateAccount() throws Exception {
        AccountRequest request = new AccountRequest("Saving Account 2", AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated());

        List<Account> accs = accountRepository.findByUserEmail("test@gmail.com");


        assertThat(accs.size()).isEqualTo(3);

        Account saved = accountRepository.findByNameAndUserEmail("Saving Account 2", "test@gmail.com");

        assertThat(saved).isNotNull();

        assertThat(saved.getName()).isEqualTo("Saving Account 2");
        assertThat(saved.getAccountType()).isEqualTo(AccountType.SAVINGS);
        assertThat(saved.getUser()).isEqualTo(testUser);


    }

    @Test
    void shouldFailCreateAccount_NotLoggedIn() throws Exception {
        AccountRequest request = new AccountRequest("Saving Account", AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailCreateAccount_BadRequest() throws Exception {
        AccountRequest request = new AccountRequest(null, AccountType.SAVINGS);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAllUserAccounts() throws Exception {
        mockMvc.perform(get("/api/v1/accounts")
                .with(csrf()))
                .andExpect(status().isOk());

        List<Account> accounts = accountRepository.findByUserEmail("test@gmail.com");

        assertThat(accounts.size()).isEqualTo(2);



        assertThat(accountRepository.findByNameAndUserEmail("Savings Account","test@gmail.com")).isEqualTo(testAccount1);
        assertThat(accountRepository.findByNameAndUserEmail("Credit Account","test@gmail.com")).isEqualTo(testAccount2);
    }

    @Test
    void shouldFailToGetAllUserAccounts_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());

    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAccountBalance() throws Exception {
        Category category = Category.createCategory("savings", CategoryType.CREDIT,testUser);
        categoryRepository.save(category);

        Transaction transaction =Transaction.createTransaction(BigDecimal.valueOf(4000), TransactionType.CREDIT, LocalDate.now(),"Bought Laptop",testAccount1,category);
        transactionRepository.save(transaction);

        Account acc = accountRepository.findByNameAndUserEmail("Savings Account","test@gmail.com");

        assertThat(acc).isNotNull();

        Long id = acc.getId();

        mockMvc.perform(get("/api/v1/accounts/{id}/balance",id)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(4000));

    }

}
