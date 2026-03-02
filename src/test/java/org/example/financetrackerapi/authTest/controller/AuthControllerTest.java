package org.example.financetrackerapi.authTest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.auth.*;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerTest.MockConfig.class)
public class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AuthService service;
    @Autowired
    private ObjectMapper mapper;


    @TestConfiguration
    static class MockConfig{
        @Bean
        AuthService authService(){
            return mock(AuthService.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }


    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldRegisterUser_Sucecefully() throws Exception {
        RegisterRequest request = new RegisterRequest("test@gmail.com","testPass");
        AuthResponse response = new AuthResponse("test@gmail.com","testPass");
        when(service.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@gmail.com"));


    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailRegistering_FailValidation() throws Exception {
        RegisterRequest request = new RegisterRequest(null,"testPass");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldLogin_Successfully() throws Exception {
        LoginRequest request = new LoginRequest("test@gmail.com","testPass");
        LoginResponse response = new LoginResponse("fake-token","Bearer");
        when(service.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-token"));

    }

    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"})
    void shouldFailLogin_FailValidation() throws Exception {
        LoginRequest request = new LoginRequest(null,"testPass");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldFailLogin_Unauthorized() throws Exception {
        LoginRequest request = new LoginRequest("tester1@gmail.com","testPass");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }


}
