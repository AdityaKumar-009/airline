package com.airline.auth.controller;

import com.airline.auth.dto.AuthDtos.*;
import com.airline.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse register(@Valid @RequestBody RegisterRequest r) {
    return service.register(r);
  }

  @PostMapping("/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest r) {
    return service.login(r);
  }

  @GetMapping("/me")
  @SecurityRequirement(name = "bearerAuth")
  public UserResponse me(JwtAuthenticationToken auth) {
    return service.me(Long.valueOf(auth.getToken().getSubject()));
  }

  @PatchMapping("/me")
  @SecurityRequirement(name = "bearerAuth")
  public UserResponse updateMe(
      JwtAuthenticationToken auth, @Valid @RequestBody UpdateProfileRequest request) {
    return service.updateMe(Long.valueOf(auth.getToken().getSubject()), request);
  }

  @GetMapping("/admin/customers")
  @SecurityRequirement(name = "bearerAuth")
  public List<AdminCustomerResponse> customers(@RequestParam(required = false) String search) {
    return service.customers(search);
  }
}
