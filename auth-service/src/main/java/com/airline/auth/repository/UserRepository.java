package com.airline.auth.repository;
import com.airline.auth.entity.User;
import java.util.Optional;
import java.util.List;
import com.airline.auth.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User,Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
  List<User> findByRoleOrderByCreatedAtDesc(UserRole role);

  @Query("select u from User u where u.role = :role and "
      + "(lower(u.name) like lower(concat('%', :search, '%')) "
      + "or lower(u.email) like lower(concat('%', :search, '%'))) "
      + "order by u.createdAt desc")
  List<User> searchByRole(@Param("role") UserRole role, @Param("search") String search);
}
