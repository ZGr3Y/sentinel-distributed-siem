package com.sentinel.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for authentication with Bean Validation.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
