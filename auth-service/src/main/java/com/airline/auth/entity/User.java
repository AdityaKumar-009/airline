package com.airline.auth.entity;

import com.airline.auth.domain.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Clock;
import java.time.LocalDateTime;

@Entity @Table(name="users")
public class User {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
  @Column(nullable=false, length=100) public String name;
  @Column(nullable=false, unique=true, length=150) public String email;
  @Column(name="password_hash", nullable=false) public String passwordHash;
  @Enumerated(EnumType.STRING)
  @Column(nullable=false, length=20)
  public UserRole role;
  @Column(name="created_at",nullable=false) public LocalDateTime createdAt;
  protected User() {}
  public User(String name, String email, String passwordHash, UserRole role) {
    this.name=name;this.email=email;this.passwordHash=passwordHash;this.role=role;
    this.createdAt=LocalDateTime.now(Clock.systemUTC());
  }
}
