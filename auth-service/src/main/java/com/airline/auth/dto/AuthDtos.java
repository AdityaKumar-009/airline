package com.airline.auth.dto;

import com.airline.auth.domain.UserRole;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class AuthDtos {
  public record RegisterRequest(@NotBlank @Size(max=100) String name, @NotBlank @Email @Size(max=150) String email, @NotBlank @Size(min=8,max=72) String password) {}
  public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
  public record UpdateProfileRequest(@NotBlank @Size(max=100) String name) {}
  public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min=8,max=72) String newPassword) {}
  public record AuthResponse(String token, String name, UserRole role) {}
  public record UserResponse(Long id, String name, String email, UserRole role) {}
  public record AdminCustomerResponse(Long id, String name, String email, LocalDateTime createdAt) {}
}

