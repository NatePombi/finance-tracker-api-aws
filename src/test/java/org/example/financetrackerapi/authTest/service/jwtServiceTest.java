package org.example.financetrackerapi.authTest.service;

import io.jsonwebtoken.ExpiredJwtException;
import org.example.financetrackerapi.auth.JwtService;
import org.example.financetrackerapi.user.User;
import org.example.financetrackerapi.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class jwtServiceTest {
    private JwtService jwtService;

    private final String secrets = Base64.getEncoder().encodeToString("c3VwZXItc2VjcmV0LWtleS1zdXBlci1zZWNyZXQta2V5LXN1cGVyLXNlY3JldC1rZXk=".getBytes());

    private long expiration = 1000*60*60;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secrets", secrets);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
    }


    @Test
    void shouldGenerateToken_Successfully() {

        User user = User.create("test@gmail.com","testPass");

        String token = jwtService.generateToken(user);

        assertThat(token).isNotNull();

        assertThat(jwtService.extractUsername(token)).isEqualTo("test@gmail.com");
        assertThat(jwtService.extractRole(token)).isEqualTo(UserRole.USER.name());
    }

    @Test
    void shouldValidateToken_Successfully() {
        User user = User.create("test@gmail.com","testPass");

        String token = jwtService.generateToken(user);


        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@gmail.com")
                .password("testPass")
                .roles("USER")
                .build();

        boolean validate = jwtService.isTokenValid(token,userDetails);

        assertThat(validate).isTrue();


    }


    @Test
    void shouldFailValidateToken_WrongUser(){
        User user = User.create("test@gmail.com","testPass");
        String token = jwtService.generateToken(user);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("wrongUser@gmail.com")
                .password("testPass")
                .roles("USER")
                .build();

        boolean validate = jwtService.isTokenValid(token,userDetails);

        assertThat(validate).isFalse();
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        User user = User.create("test@gmail.com","testPass");
        ReflectionTestUtils.setField(jwtService, "expiration", 1L);
        String token = jwtService.generateToken(user);
        Thread.sleep(5);

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@gmail.com")
                .password("testPass")
                .roles("USER")
                .build();


        assertThatThrownBy(()-> jwtService.isTokenValid(token,userDetails)).isInstanceOf(ExpiredJwtException.class);

    }

}
