package org.example.financetrackerapi.authTest.service;

import org.example.financetrackerapi.auth.dto.AuthResponse;
import org.example.financetrackerapi.auth.dto.LoginRequest;
import org.example.financetrackerapi.auth.dto.LoginResponse;
import org.example.financetrackerapi.auth.dto.RegisterRequest;
import org.example.financetrackerapi.auth.service.AuthService;
import org.example.financetrackerapi.auth.service.JwtService;
import org.example.financetrackerapi.exception.BadCredentialException;
import org.example.financetrackerapi.exception.EmailAlreadyExistException;
import org.example.financetrackerapi.user.entity.User;
import org.example.financetrackerapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserRepository userRepo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService service;
    @InjectMocks
    private AuthService authService;

    public User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.create("test@gmail.com", "hashed-password");
    }

    @Test
    void shouldRegisterUser_Successfully() {
        RegisterRequest request = new RegisterRequest("test@gmail.com", "password");
        when(userRepo.existsByEmail(request.getEmail())).thenReturn(Boolean.FALSE);
        when(encoder.encode(request.getPassword())).thenReturn("hashed-password");
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.register(request);

        assertNotNull(response);

        assertEquals(request.getEmail(), response.email(),"should be the same email");

        verify(userRepo).save(any(User.class));

    }

    @Test
    void shouldFailToRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@gmail.com", "password");
        when(userRepo.existsByEmail(request.getEmail())).thenReturn(Boolean.TRUE);

        assertThrows(EmailAlreadyExistException.class,()->{
            authService.register(request);
        });


        verify(userRepo,never()).save(any(User.class));
    }

    @Test
    void shouldLogin_Successfully() {
        User user = User.create("test", "password");
        LoginRequest request = new LoginRequest("test", "password");
        when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(encoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        LoginResponse response = authService.login(request);

        assertNotNull(response);

        assertEquals(LoginResponse.class, response.getClass(), "should be the same class");


    }

    @Test
    void shouldFailToLogin_BadCredentialsException_IncorrectPassword() {
        LoginRequest request = new LoginRequest("test@gmail.com", "incorrect");
        when(userRepo.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
        when(encoder.matches(request.getPassword(), testUser.getPassword())).thenReturn(false);
        assertThrows(BadCredentialException.class,()->{
            authService.login(request);
        });

        verify(userRepo,never()).save(any(User.class));
    }

    @Test
    void shouldFailToLogin_BadCredentialsException_IncorrectEmail() {
        LoginRequest request = new LoginRequest("test@gmail.com", "hashed-password");
        assertThrows(BadCredentialException.class,()->{
            authService.login(request);
        });

        verify(userRepo,never()).save(any(User.class));
    }
}
