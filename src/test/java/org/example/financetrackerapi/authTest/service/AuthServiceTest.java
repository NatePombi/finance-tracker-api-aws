package org.example.financetrackerapi.authTest.service;

import org.example.financetrackerapi.auth.*;
import org.example.financetrackerapi.exception.EmailAlreadyExistException;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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

    @Test
    void shouldRegisterUser_Successfully() {
        RegisterRequest request = new RegisterRequest("test", "password");
        when(userRepo.existsByEmail(request.getEmail())).thenReturn(Boolean.FALSE);
        when(encoder.encode(request.getPassword())).thenReturn("password");

        AuthResponse response = authService.register(request);

        assertNotNull(response);

        assertEquals(request.getEmail(), response.getEmail(),"should be the same email");


    }

    @Test
    void shouldFailToRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test", "password");
        when(userRepo.existsByEmail(request.getEmail())).thenReturn(Boolean.TRUE);

        assertThrows(EmailAlreadyExistException.class,()->{
            authService.register(request);
        });
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
    void shouldFailToLogin_BadCredentialsException() {
        LoginRequest request = new LoginRequest(null, null);
        assertThrows(BadCredentialsException.class,()->{
            authService.login(request);
        });
    }
}
