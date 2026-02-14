package org.example.financetrackerapi.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponse {
    private String token;
    private String tokenType;
}
