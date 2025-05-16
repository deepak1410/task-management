package com.deeptechhub.identityservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank private final String username;
    @NotBlank private final String password;
    @Email @NotBlank private final String email;
    @NotBlank private final String name;
}
