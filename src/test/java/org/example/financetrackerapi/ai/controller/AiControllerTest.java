package org.example.financetrackerapi.ai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.ai.AiController;
import org.example.financetrackerapi.ai.AiService;
import org.example.financetrackerapi.auth.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AiController.class)
@Import(AiControllerTest.AiTestConfiguration.class)
public class AiControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private AiService service;


    @TestConfiguration
    static class AiTestConfiguration {
        @Bean
        AiService aiService() {
            return mock(AiService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }

    @Test
    @WithMockUser(username = "test@gmail.com",roles = {"USER"})
    void shouldGetAiAnalyze() throws Exception {
        String prompt = service.getTransactionsForAiAnalysis("test@gmail.com");
        when(service.getTransactionsForAiAnalysis("test@gmail.com")).thenReturn(anyString());

        when(service.analyzeSpending(prompt)).thenReturn(anyString());


        mvc.perform(get("/api/v1/ai/analyze")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}
