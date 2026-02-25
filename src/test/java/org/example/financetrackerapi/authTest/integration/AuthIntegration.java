package org.example.financetrackerapi.authTest.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.financetrackerapi.auth.LoginRequest;
import org.example.financetrackerapi.auth.RegisterRequest;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class AuthIntegration {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private UserRepository repo;
    @Autowired
    private PasswordEncoder encoder;

    @Test
    void shouldRegisterUser_Successfully() throws Exception {
        RegisterRequest request = new RegisterRequest("test@gmail.com","testPass");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated());

        Optional<User> saved = repo.findByEmail("test@gmail.com");

        assertTrue(saved.isPresent());
    }

    @Test
    void shouldNotRegisterUser_WhenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("","testPass");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldNotRegisterUser_WhenPasswordIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("test@gmail.com","");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }


    @Test
    void shouldLoginUser_Successfully() throws Exception {
        User user = User.create("test@gmail.com", encoder.encode("testPass"));
        LoginRequest request = new LoginRequest("test@gmail.com","testPass");
        repo.save(user);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk());

        boolean fetchUser = repo.existsByEmail("test@gmail.com");

        assertTrue(fetchUser);

    }


    @Test
    void shouldLoginUser_WhenPasswordIsInvalid() throws Exception {
        User user = User.create("test@gmail.com", encoder.encode("testPass"));
        repo.save(user);
        LoginRequest request = new LoginRequest("test@gmail.com","");

        mockMvc.perform(post("/api/v1/auth/login")
                 .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void shouldLoginUser_WhenEmailIsInvalid() throws Exception {
        User user = User.create("test@gmail.com", encoder.encode("testPass"));
        repo.save(user);
        LoginRequest request = new LoginRequest("","testPass");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity());
    }
}
