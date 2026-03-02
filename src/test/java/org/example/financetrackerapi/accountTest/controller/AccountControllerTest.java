package org.example.financetrackerapi.accountTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.account.*;
import org.example.financetrackerapi.auth.JwtService;
import org.example.financetrackerapi.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(AccountControllerTest.MockConfig.class)
public class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private AccountService service;


    @TestConfiguration
    static class MockConfig{
        @Bean
        AccountService accountService(){
            return mock(AccountService.class);
        }

        @Bean
        JwtService jwtService(){
            return mock(JwtService.class);
        }
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldCreateAccount_Successfully() throws Exception {
        AccountRequest request = new AccountRequest("Savings Account", AccountType.SAVINGS);
        AccountResponse response = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);

        when(service.create(any(AccountRequest.class),any())).thenReturn(response);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.accountType").value(AccountType.SAVINGS.toString()));

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailToCreateAccount_BadCredentials() throws Exception {
        AccountRequest request = new AccountRequest(null, AccountType.SAVINGS);
        AccountResponse response = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);

        when(service.create(any(AccountRequest.class),any())).thenReturn(response);

        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());

    }

    @Test
    void shouldFailToCreateAccount_NotLoggedInUser() throws Exception {
        AccountRequest request = new AccountRequest("test@gmail.com", AccountType.SAVINGS);
        AccountResponse response = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);

        when(service.create(any(AccountRequest.class),any())).thenReturn(response);

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetUserAccounts() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");
        AccountResponse response1 = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);
        AccountResponse response2 = new AccountResponse(2L,"Credit Account",AccountType.CREDIT,2L);
        List<AccountResponse> responses = List.of(response1,response2);

        when(service.getAccounts(testUser.getEmail())).thenReturn(responses);

        mockMvc.perform(get("/accounts")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Savings Account"))
                .andExpect(jsonPath("$[0].accountType").value(AccountType.SAVINGS.toString()))
                .andExpect(jsonPath("$[1].name").value("Credit Account"))
                .andExpect(jsonPath("$[1].accountType").value(AccountType.CREDIT.toString()));
    }



    @Test
    void shouldFailGetUserAccounts_NotLoggedInUser() throws Exception {
        User testUser = User.create("test@gmail.com","testPass");
        AccountResponse response1 = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);
        AccountResponse response2 = new AccountResponse(2L,"Credit Account",AccountType.CREDIT,2L);
        List<AccountResponse> responses = List.of(response1,response2);

        when(service.getAccounts(testUser.getEmail())).thenReturn(responses);

        mockMvc.perform(get("/accounts")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldAccountBalance() throws Exception {
        BalanceResponse response = new BalanceResponse(BigDecimal.valueOf(550));
        when(service.getBalance(any(),any())).thenReturn(response);

        mockMvc.perform(get("/accounts/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(550));
    }

    @Test
    void shouldFailToGetAccountBalance() throws Exception {
        BalanceResponse response = new BalanceResponse(BigDecimal.valueOf(550));
        when(service.getBalance(any(),any())).thenReturn(response);

        mockMvc.perform(get("/accounts/1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

}
