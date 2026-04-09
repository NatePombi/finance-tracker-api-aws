package org.example.financetrackerapi.transactionTest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.With;
import org.example.financetrackerapi.account.AccountType;
import org.example.financetrackerapi.ai.AiService;
import org.example.financetrackerapi.auth.JwtService;
import org.example.financetrackerapi.transaction.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.TestConfig.class)
public class TransactionControllerTest {
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private TransactionService service;
    @Autowired
    private MockMvc mockMvc;


    @TestConfiguration
    static class TestConfig{
        @Bean
        public TransactionService transactionService(){
            return mock(TransactionService.class);
        }


        @Bean
        public JwtService jwtService(){
            return mock(JwtService.class);
        }
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldCreateTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(1200), TransactionType.DEBIT,1L,1L, LocalDate.now(),"Bought groceries");
        TransactionResponse response = new TransactionResponse(22L,new BigDecimal(1200),TransactionType.DEBIT,"Groceries",request.getDescription(),1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        when(service.create(request,"test@gmail.com")).thenReturn(response);

        mockMvc.perform(post("/api/v1/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());

    }

    @Test
    void shouldFailCreateTransaction_NotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/v1/transactions")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateTransaction_BadCredentials() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(1200), null,1L,1L, LocalDate.now(),"Bought groceries");


        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailCreateTransaction_NegativeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(new BigDecimal(-1200), TransactionType.DEBIT,1L,1L, LocalDate.now(),"Bought groceries");

        mockMvc.perform(post("/api/v1/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetTransactions() throws Exception {
        TransactionResponse response1 = new TransactionResponse(22L,new BigDecimal(1200),TransactionType.DEBIT,"Groceries","from woolworths",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        TransactionResponse response2 = new TransactionResponse(23L,new BigDecimal(600),TransactionType.DEBIT,"Groceries","from FoodLovers",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        TransactionResponse response3 = new TransactionResponse(24L,new BigDecimal(100),TransactionType.DEBIT,"Groceries","from Pick n Pay",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());

        List<TransactionResponse> list = List.of(response1,response2,response3);
        Page<TransactionResponse> page = new PageImpl<>(list);

        when(service.getTransactions(eq("test@gmail.com"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(22))
                .andExpect(jsonPath("$.content[0].amount").value(1200))
                .andExpect(jsonPath("$.content[0].description").value("from woolworths"))
                .andExpect(jsonPath("$.content[1].id").value(23))
                .andExpect(jsonPath("$.content[1].amount").value(600))
                .andExpect(jsonPath("$.content[1].description").value("from FoodLovers"))
                .andExpect(jsonPath("$.content[2].id").value(24))
                .andExpect(jsonPath("$.content[2].amount").value(100))
                .andExpect(jsonPath("$.content[2].description").value("from Pick n Pay"));
    }




    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetTransactions_ByDate() throws Exception {
        LocalDateTime created  = LocalDateTime.of(2026, 2, 9, 14, 30);

        TransactionResponse response1 = new TransactionResponse(22L,new BigDecimal(1200),TransactionType.DEBIT,"Groceries","from woolworths",1L, AccountType.CASH,LocalDate.of(2026,2,9),created);
        TransactionResponse response2 = new TransactionResponse(23L,new BigDecimal(600),TransactionType.DEBIT,"Groceries","from FoodLovers",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        TransactionResponse response3 = new TransactionResponse(24L,new BigDecimal(100),TransactionType.DEBIT,"Groceries","from Pick n Pay",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());

        List<TransactionResponse> list = List.of(response1,response2,response3);
        Page<TransactionResponse> page = new PageImpl<>(list);

        when(service.getTransactionsByDate(eq("test@gmail.com"),eq(LocalDate.of(2026,1,9)),eq(LocalDate.of(2026,2,20)), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("from", "2026-01-09")
                        .param("to", "2026-02-20")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(22))
                .andExpect(jsonPath("$.content[0].amount").value(1200))
                .andExpect(jsonPath("$.content[0].description").value("from woolworths"));
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetTransactions_FromDate() throws Exception {
        LocalDateTime created  = LocalDateTime.of(2026, 2, 9, 14, 30);

        TransactionResponse response1 = new TransactionResponse(22L,new BigDecimal(1200),TransactionType.DEBIT,"Groceries","from woolworths",1L, AccountType.CASH,LocalDate.of(2026,2,9),created);
        TransactionResponse response2 = new TransactionResponse(23L,new BigDecimal(600),TransactionType.DEBIT,"Groceries","from FoodLovers",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        TransactionResponse response3 = new TransactionResponse(24L,new BigDecimal(100),TransactionType.DEBIT,"Groceries","from Pick n Pay",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());

        List<TransactionResponse> list = List.of(response1,response2,response3);
        Page<TransactionResponse> page = new PageImpl<>(list);

        when(service.getTransactionsByFromDate(eq("test@gmail.com"),eq(LocalDate.of(2026,3,1)), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("from", "2026-03-01")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[1].id").value(23))
                .andExpect(jsonPath("$.content[1].amount").value(600))
                .andExpect(jsonPath("$.content[1].description").value("from FoodLovers"))
                .andExpect(jsonPath("$.content[2].id").value(24))
                .andExpect(jsonPath("$.content[2].amount").value(100))
                .andExpect(jsonPath("$.content[2].description").value("from Pick n Pay"));
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetTransactions_ToDate() throws Exception {
        LocalDateTime created  = LocalDateTime.of(2026, 2, 9, 14, 30);

        TransactionResponse response1 = new TransactionResponse(22L,new BigDecimal(1200),TransactionType.DEBIT,"Groceries","from woolworths",1L, AccountType.CASH,LocalDate.of(2026,2,9),created);
        TransactionResponse response2 = new TransactionResponse(23L,new BigDecimal(600),TransactionType.DEBIT,"Groceries","from FoodLovers",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());
        TransactionResponse response3 = new TransactionResponse(24L,new BigDecimal(100),TransactionType.DEBIT,"Groceries","from Pick n Pay",1L, AccountType.CASH,LocalDate.now(),LocalDateTime.now());

        List<TransactionResponse> list = List.of(response1,response2,response3);
        Page<TransactionResponse> page = new PageImpl<>(list);

        when(service.getTransactionByToDate(eq("test@gmail.com"),eq(LocalDate.of(2026,2,20)), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("to", "2026-02-20")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(22))
                .andExpect(jsonPath("$.content[0].amount").value(1200))
                .andExpect(jsonPath("$.content[0].description").value("from woolworths"));
    }


    @Test
    void shouldFailGetTransactions_NotLoggedIn0() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .with(csrf()))
                        .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailGetTransactions_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions")
                        .param("from", "2026-01-09")
                        .param("to", "invalid")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetMonthlyTransactions() throws Exception {
        MonthlySummaryReport report = new MonthlySummaryReport(2026,3,new BigDecimal(2000),new BigDecimal(1000),new BigDecimal(1000));

        when(service.getMonthlySummaryReport(eq("test@gmail.com"),eq(2026),eq(3))).thenReturn(report);

        mockMvc.perform(get("/api/v1/transactions/summary")
                .param("year", "2026")
                .param("month", "3")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(3))
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.totalIncome").value(new BigDecimal(2000)))
                .andExpect(jsonPath("$.totalExpense").value(new BigDecimal(1000)))
                .andExpect(jsonPath("$.totalBalance").value(new BigDecimal(1000)));
    }

    @Test
    void shouldFailGetMonthlyTransactions_NotLoggedIn() throws Exception {

        mockMvc.perform(get("/api/v1/transactions/summary")
                .param("year", "2026")
                .param("month", "3")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailGetMonthlyTransactions_BadRequest() throws Exception {

        mockMvc.perform(get("/api/v1/transactions/summary")
                .param("year", "2026")
                .param("month", "invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());

    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldGetCategorySummary() throws Exception {
        CategorySummaryResponse categorySummaryResponse1 = new CategorySummaryResponse("Savings",new BigDecimal(15000));
        CategorySummaryResponse categorySummaryResponse2 = new CategorySummaryResponse("Loan",new BigDecimal(500));
        CategorySummaryResponse categorySummaryResponse3 = new CategorySummaryResponse("Groceries",new BigDecimal(2000));

        List<CategorySummaryResponse> responseList = List.of(categorySummaryResponse1,categorySummaryResponse2,categorySummaryResponse3);

        when(service.getCategorySummaryResponse(eq("test@gmail.com"),eq(2026),eq(3))).thenReturn(responseList);

        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year", "2026")
                .param("month", "3")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].category").value("Savings"))
                .andExpect(jsonPath("$.[0].total").value(new BigDecimal(15000)))
                .andExpect(jsonPath("$.[1].category").value("Loan"))
                .andExpect(jsonPath("$.[1].total").value(new BigDecimal(500)))
                .andExpect(jsonPath("$.[2].category").value("Groceries"))
                .andExpect(jsonPath("$.[2].total").value(new BigDecimal(2000)));
    }

    @Test
    void shouldFailGetCategorySummary_NotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year", "2026")
                .param("month", "3")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldFailGetCategorySummary_BadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/summary/category")
                .param("year", "2026")
                .param("month", "invalid")
                .with(csrf()))
                .andExpect(status().isBadRequest());
    }




}
