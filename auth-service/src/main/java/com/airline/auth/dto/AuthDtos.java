package com.airline.auth.dto;

import com.airline.auth.domain.UserRole;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class AuthDtos {
  public record RegisterRequest(@NotBlank String name, @NotBlank @Email String email, @NotBlank @Size(min=8) String password) {}
  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
  public record UpdateProfileRequest(@NotBlank @Size(max=100) String name) {}
  public record AuthResponse(String token, String name, UserRole role) {}
  public record UserResponse(Long id, String name, String email, UserRole role) {}
  public record AdminCustomerResponse(Long id, String name, String email, LocalDateTime createdAt) {}
}
