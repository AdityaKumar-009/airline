package com.airline.auth.service;

import com.airline.auth.domain.UserRole;
import com.airline.auth.entity.User;
import com.airline.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DemoUsers {
  @Bean
  CommandLineRunner seedUsers(
      UserRepository users,
      PasswordEncoder passwords,
      @Value("${demo.admin.password}") String adminPassword,
      @Value("${demo.customer.password}") String customerPassword) {
    return args -> {
      addUserIfMissing(
          users, passwords, "Demo Admin", "admin@airline.test", adminPassword, UserRole.ADMIN);
      addUserIfMissing(
          users,
          passwords,
          "Demo Customer",
          "customer@airline.test",
          customerPassword,
          UserRole.CUSTOMER);
    };
  }

  private void addUserIfMissing(
      UserRepository users,
      PasswordEncoder passwords,
      String name,
      String email,
      String password,
      UserRole role) {
    if (users.existsByEmail(email)) {
      return;
    }

    String passwordHash = passwords.encode(password);
    users.save(new User(name, email, passwordHash, role));
  }
}
