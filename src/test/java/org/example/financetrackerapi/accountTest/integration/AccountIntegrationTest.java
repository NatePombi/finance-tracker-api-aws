package org.example.financetrackerapi.accountTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.account.Account;
import org.example.financetrackerapi.account.AccountRepository;
import org.example.financetrackerapi.account.AccountRequest;
import org.example.financetrackerapi.account.AccountType;
import org.example.financetrackerapi.category.Category;
import org.example.financetrackerapi.category.CategoryRepository;
import org.example.financetrackerapi.category.CategoryType;
import org.example.financetrackerapi.transaction.Transaction;
import org.example.financetrackerapi.transaction.TransactionRepository;
import org.example.financetrackerapi.transaction.TransactionType;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldCreateAccount() throws Exception {
        User user = User.create("test@gmail.com",encoder.encode("test"));
        userRepository.save(user);
        AccountRequest request = new AccountRequest("Saving Account", AccountType.SAVINGS);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());

        assertThat(request).isNotNull();

        assertThat(request.getAccountName()).isEqualTo("Saving Account");
        assertThat(request.getAccountType()).isEqualTo(AccountType.SAVINGS);
    }

    @Test
    void shouldFailCreateAccount_NotLoggedIn() throws Exception {
        AccountRequest request = new AccountRequest("Saving Account", AccountType.SAVINGS);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailCreateAccount_BadCredentials() throws Exception {
        AccountRequest request = new AccountRequest(null, AccountType.SAVINGS);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAllUserAccounts() throws Exception {
        User user = User.create("test@gmail.com",encoder.encode("test"));
        userRepository.save(user);

        Account account = Account.create("Savings Account", AccountType.SAVINGS, user);
        Account account1 = Account.create("Credit Account", AccountType.CREDIT, user);
        accountRepository.save(account);
        accountRepository.save(account1);

        mockMvc.perform(get("/accounts")
                .with(csrf()))
                .andExpect(status().isOk());

        List<Account> accounts = accountRepository.findAll();

        assertThat(accounts.size()).isEqualTo(2);
        assertThat(accounts.get(0)).isEqualTo(account);
        assertThat(accounts.get(1)).isEqualTo(account1);
    }

    @Test
    void shouldFailToGetAllUserAccounts_NotLoggedIn() throws Exception {
        User user = User.create("test@gmail.com",encoder.encode("test"));
        userRepository.save(user);

        Account account = Account.create("Savings Account", AccountType.SAVINGS, user);
        Account account1 = Account.create("Credit Account", AccountType.CREDIT, user);
        accountRepository.save(account);
        accountRepository.save(account1);

        mockMvc.perform(get("/accounts")
                        .with(csrf()))
                .andExpect(status().isForbidden());

    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAccountBalance() throws Exception {
        User user = User.create("test@gmail.com",encoder.encode("test"));
        userRepository.save(user);


        Account account = Account.create("Savings Account", AccountType.SAVINGS, user);
        accountRepository.save(account);

        Category category = Category.createCategory("savings", CategoryType.CREDIT,user);
        categoryRepository.save(category);

        Transaction transaction =Transaction.createTransaction(BigDecimal.valueOf(4000), TransactionType.CREDIT, LocalDate.now(),"Bought Laptop",account,category);
        transactionRepository.save(transaction);


        mockMvc.perform(get("/accounts/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(4000));

    }

}
