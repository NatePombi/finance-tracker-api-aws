package org.example.financetrackerapi.auth.dto;


public record AuthResponse(
        Long id,
        String email,
        String message
) {}
