package org.example.financetrackerapi.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Login Request")
@Getter
@AllArgsConstructor
public class LoginRequest {
    @Email
    @Schema(description = "Email that you registered with", example = "test@gmail.com")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @Schema(description = "Password that you registered with", example = "testPass123")
    @Size(min = 7, message = "Password must be at lease 7 characters long")
    private String password;
}
