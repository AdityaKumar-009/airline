package com.airline.auth.service;

import com.airline.auth.domain.UserRole;
import com.airline.auth.dto.AuthDtos.AuthResponse;
import com.airline.auth.dto.AuthDtos.AdminCustomerResponse;
import com.airline.auth.dto.AuthDtos.LoginRequest;
import com.airline.auth.dto.AuthDtos.RegisterRequest;
import com.airline.auth.dto.AuthDtos.UpdateProfileRequest;
import com.airline.auth.dto.AuthDtos.UserResponse;
import com.airline.auth.entity.User;
import com.airline.auth.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {
  private static final Duration TOKEN_LIFETIME = Duration.ofHours(1);
  private static final String TOKEN_ISSUER = "airline-auth";

  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final JwtEncoder jwtEncoder;

  public AuthServiceImpl(UserRepository users, PasswordEncoder passwords, JwtEncoder jwtEncoder) {
    this.users = users;
    this.passwords = passwords;
    this.jwtEncoder = jwtEncoder;
  }

  @Override
  public UserResponse register(RegisterRequest request) {
    String email = normalizeEmail(request.email());
    if (users.existsByEmail(email)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
    }

    User user = new User(
        request.name().trim(), email, passwords.encode(request.password()), UserRole.CUSTOMER);
    return toResponse(users.save(user));
  }

  @Override
  public AuthResponse login(LoginRequest request) {
    String email = normalizeEmail(request.email());
    User user = users.findByEmail(email).orElseThrow(this::invalidCredentials);

    if (!passwords.matches(request.password(), user.passwordHash)) {
      throw invalidCredentials();
    }

    Instant issuedAt = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(TOKEN_ISSUER)
        .subject(user.id.toString())
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plus(TOKEN_LIFETIME))
        .claim("role", user.role.name())
        .claim("email", user.email)
        .claim("name", user.name)
        .build();

    JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
    String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    return new AuthResponse(token, user.name, user.role);
  }

  @Override
  public UserResponse me(Long id) {
    User user = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    return toResponse(user);
  }

  @Override
  public UserResponse updateMe(Long id, UpdateProfileRequest request) {
    User user = users.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    user.name = request.name().trim();
    return toResponse(users.save(user));
  }

  @Override
  public void changePassword(Long id, com.airline.auth.dto.AuthDtos.ChangePasswordRequest request) {
    User user = users.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (!passwords.matches(request.currentPassword(), user.passwordHash)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
    }
    if (passwords.matches(request.newPassword(), user.passwordHash)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different");
    }
    user.passwordHash = passwords.encode(request.newPassword());
    users.save(user);
  }

  @Override
  public List<AdminCustomerResponse> customers(String search) {
    String term = search == null ? "" : search.trim();
    List<User> results = term.isEmpty()
        ? users.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER)
        : users.searchByRole(UserRole.CUSTOMER, term);
    return results.stream()
        .map(user -> new AdminCustomerResponse(user.id, user.name, user.email, user.createdAt))
        .toList();
  }

  private String normalizeEmail(String email) {
    return email.trim().toLowerCase(Locale.ROOT);
  }

  private ResponseStatusException invalidCredentials() {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(user.id, user.name, user.email, user.role);
  }
}
