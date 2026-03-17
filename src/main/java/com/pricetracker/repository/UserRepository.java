package com.pricetracker.repository;

import com.pricetracker.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @EntityGraph(attributePaths = {"trackedProducts"})
  Optional<User> findWithProductsById(Long id);
}