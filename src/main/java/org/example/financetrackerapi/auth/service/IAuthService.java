package org.example.financetrackerapi.auth.service;

import org.example.financetrackerapi.auth.dto.AuthResponse;
import org.example.financetrackerapi.auth.dto.LoginRequest;
import org.example.financetrackerapi.auth.dto.LoginResponse;
import org.example.financetrackerapi.auth.dto.RegisterRequest;

public interface IAuthService {
    AuthResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
}
