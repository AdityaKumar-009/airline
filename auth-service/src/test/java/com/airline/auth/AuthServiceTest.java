package com.airline.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.airline.auth.domain.UserRole;
import com.airline.auth.dto.AuthDtos.UpdateProfileRequest;
import com.airline.auth.entity.User;
import com.airline.auth.repository.UserRepository;
import com.airline.auth.service.AuthServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

class AuthServiceTest {
  private final UserRepository users = org.mockito.Mockito.mock(UserRepository.class);
  private final AuthServiceImpl service = new AuthServiceImpl(
      users,
      org.mockito.Mockito.mock(PasswordEncoder.class),
      org.mockito.Mockito.mock(JwtEncoder.class));

  @Test
  void customersOnlyReturnsCustomerSafeFields() {
    User customer = new User("Casey Customer", "casey@example.test", "secret-hash", UserRole.CUSTOMER);
    customer.id = 4L;
    when(users.findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER)).thenReturn(List.of(customer));

    var result = service.customers(" ");

    assertEquals(1, result.size());
    assertEquals(4L, result.get(0).id());
    assertEquals("casey@example.test", result.get(0).email());
    verify(users).findByRoleOrderByCreatedAtDesc(UserRole.CUSTOMER);
  }

  @Test
  void profileUpdateTrimsNameAndKeepsEmailAndRole() {
    User customer = new User("Old Name", "casey@example.test", "secret-hash", UserRole.CUSTOMER);
    customer.id = 4L;
    when(users.findById(4L)).thenReturn(Optional.of(customer));
    when(users.save(customer)).thenReturn(customer);

    var result = service.updateMe(4L, new UpdateProfileRequest("  Casey New  "));

    assertEquals("Casey New", result.name());
    assertEquals("casey@example.test", result.email());
    assertEquals(UserRole.CUSTOMER, result.role());
    verify(users).save(customer);
  }
}
