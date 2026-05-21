package org.example.financetrackerapi.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.example.financetrackerapi.auth.dto.AuthResponse;
import org.example.financetrackerapi.auth.dto.LoginRequest;
import org.example.financetrackerapi.auth.dto.LoginResponse;
import org.example.financetrackerapi.auth.dto.RegisterRequest;
import org.example.financetrackerapi.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "Operation managing user Registration and logins")
@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Registers a new User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered , returns success message"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "404", description = "Not Found")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Logging in User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in, returns Jwt Token"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse( responseCode = "404" , description = "Not Found")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(authService.login(request));
    }
}
