package org.example.financetrackerapi.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
@Schema(description = "Register Request")
@Getter
@AllArgsConstructor
public class RegisterRequest {
    @Schema(description = "Email that you will be using to log in with", example = "test@gmail.com")
    @Email(message = "Must be valid email format")
    @NotBlank(message = "Email Cannot be empty")
    private String email;
    @Schema(description = "Password that you will be using to log in with", example = "testPass123")
    @Length(min = 6,message = "Must be 6 characters long")
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
