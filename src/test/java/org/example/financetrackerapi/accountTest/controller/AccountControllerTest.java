package org.example.financetrackerapi.accountTest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.account.controller.AccountController;
import org.example.financetrackerapi.account.dto.AccountRequest;
import org.example.financetrackerapi.account.dto.AccountResponse;
import org.example.financetrackerapi.account.dto.BalanceResponse;
import org.example.financetrackerapi.account.entity.Account;
import org.example.financetrackerapi.account.entity.TestAccount;
import org.example.financetrackerapi.account.enums.AccountType;
import org.example.financetrackerapi.account.service.AccountService;
import org.example.financetrackerapi.auth.service.JwtService;
import org.example.financetrackerapi.exception.AccountNotFoundException;
import org.example.financetrackerapi.user.entity.TestUser;
import org.example.financetrackerapi.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

        when(service.create(any(AccountRequest.class),eq("test@gmail.com"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Savings Account"))
                .andExpect(jsonPath("$.accountType").value(AccountType.SAVINGS.toString()));

        verify(service).create(any(AccountRequest.class),eq("test@gmail.com"));

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailToCreateAccount_BadRequest() throws Exception {
        AccountRequest request = new AccountRequest(null, AccountType.SAVINGS);


        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isBadRequest());

    }

    @Test
    void shouldFailToCreateAccount_NotLoggedInUser() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }




    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetUserAccounts() throws Exception {
        AccountResponse response1 = new AccountResponse(1L,"Savings Account",AccountType.SAVINGS,2L);
        AccountResponse response2 = new AccountResponse(2L,"Credit Account",AccountType.CREDIT,2L);
        Page<AccountResponse> page = new PageImpl<>(List.of(response1,response2), PageRequest.of(0,5),2);

        when(service.getAccounts("test@gmail.com",0,5,"id","desc")).thenReturn(page);

        mockMvc.perform(get("/api/v1/accounts")
                        .param("page","0")
                        .param("size","5")
                        .param("sortBy","id")
                        .param("direction","desc"))
                .andExpect(status().isOk());

        Page<AccountResponse> responses = service.getAccounts("test@gmail.com",0,5,"id","desc");

        verify(service,atLeast(1)).getAccounts("test@gmail.com",0,5,"id","desc");

        assertThat(responses.getContent().size()).isEqualTo(2);
    }



    @Test
    void shouldFailGetUserAccounts_NotLoggedInUser() throws Exception {

        mockMvc.perform(get("/api/v1/accounts"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAccountBalance() throws Exception {
        BalanceResponse response = new BalanceResponse(BigDecimal.valueOf(550));
        when(service.getBalance("test@gmail.com",1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/accounts/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(550));

        verify(service).getBalance("test@gmail.com",1L);
    }

    @Test
    void shouldFailToGetAccountBalance_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/v1/accounts/1/balance"))
                .andExpect(status().isUnauthorized());
    }

}
