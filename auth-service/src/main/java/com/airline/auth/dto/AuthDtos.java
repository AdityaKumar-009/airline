package com.airline.auth.dto;

import com.airline.auth.domain.UserRole;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class AuthDtos {
  private static final String NAME_PATTERN = "^ *[\\p{L}]+(?: +[\\p{L}]+)* *$";
  private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+\\.[A-Za-z]{2,}$";

  public record RegisterRequest(
      @NotBlank @Pattern(regexp=NAME_PATTERN) @Size(max=100) String name,
      @NotBlank @Email @Pattern(regexp=EMAIL_PATTERN) @Size(max=150) String email,
      @NotBlank @Size(min=8,max=72) String password) {}

  public record LoginRequest(
      @NotBlank @Email @Pattern(regexp=EMAIL_PATTERN) String email,
      @NotBlank String password) {}

  public record UpdateProfileRequest(
      @NotBlank @Pattern(regexp=NAME_PATTERN) @Size(max=100) String name) {}
  public record ChangePasswordRequest(@NotBlank String currentPassword, @NotBlank @Size(min=8,max=72) String newPassword) {}
  public record AuthResponse(String token, String name, UserRole role) {}
  public record UserResponse(Long id, String name, String email, UserRole role) {}
  public record AdminCustomerResponse(Long id, String name, String email, LocalDateTime createdAt) {}
}

