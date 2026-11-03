package com.airline.auth.service;

import com.airline.auth.dto.AuthDtos.AuthResponse;
import com.airline.auth.dto.AuthDtos.LoginRequest;
import com.airline.auth.dto.AuthDtos.RegisterRequest;
import com.airline.auth.dto.AuthDtos.UpdateProfileRequest;
import com.airline.auth.dto.AuthDtos.UserResponse;
import com.airline.auth.dto.AuthDtos.AdminCustomerResponse;
import java.util.List;
public interface AuthService {
  UserResponse register(RegisterRequest request);

  AuthResponse login(LoginRequest request);

  UserResponse me(Long id);

  UserResponse updateMe(Long id, UpdateProfileRequest request);

  List<AdminCustomerResponse> customers(String search);

  void changePassword(Long id, com.airline.auth.dto.AuthDtos.ChangePasswordRequest request);
}
