package org.example.financetrackerapi.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.validator.constraints.Length;
@Schema(description = "Login Request")
@Getter
@AllArgsConstructor
public class LoginRequest {
    @Email
    @Schema(description = "Email that you registered with", example = "test@gmail.com")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @Schema(description = "Password that you registered with", example = "testPass123")
    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6)
    private String password;
}
