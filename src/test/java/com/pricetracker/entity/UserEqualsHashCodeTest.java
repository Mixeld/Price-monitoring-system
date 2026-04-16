package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для User entity - Дополнительные тесты для equals и hashCode")
class UserEqualsHashCodeTest {

  private User user;
  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    product1 = new Product();
    product1.setId(101L);
    product1.setName("iPhone 15");

    product2 = new Product();
    product2.setId(102L);
    product2.setName("MacBook Pro");

    user = new User();
    user.setId(1L);
    user.setUsername("john_doe");
    user.setEmail("john@example.com");
    user.setFullName("John Doe");
    user.setPasswordHash("hashed_password_123");
    user.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
    user.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
    user.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));
    user.setTrackedProducts(new ArrayList<>(List.of(product1, product2)));
  }

  // ==================== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ equals ====================

  @Nested
  @DisplayName("Дополнительные тесты метода equals")
  class EqualsAdditionalTests {

    @Test
    @DisplayName("[УСПЕХ] equals возвращает true для объектов с одинаковыми значениями всех полей")
    void equals_shouldReturnTrue_whenAllFieldsEqual() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setFullName("John Doe");
      user1.setPasswordHash("hash");
      user1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
      user1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
      user1.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));
      user1.setTrackedProducts(List.of(product1));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setFullName("John Doe");
      user2.setPasswordHash("hash");
      user2.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
      user2.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
      user2.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));
      user2.setTrackedProducts(List.of(product1));

      assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных id")
    void equals_shouldReturnFalse_whenDifferentId() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");

      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных username")
    void equals_shouldReturnFalse_whenDifferentUsername() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("jane_doe");
      user2.setEmail("john@example.com");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных email")
    void equals_shouldReturnFalse_whenDifferentEmail() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("jane@example.com");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных fullName")
    void equals_shouldReturnFalse_whenDifferentFullName() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setFullName("John Doe");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setFullName("Jonathan Doe");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных passwordHash")
    void equals_shouldReturnFalse_whenDifferentPasswordHash() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setPasswordHash("hash1");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setPasswordHash("hash2");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных createdAt")
    void equals_shouldReturnFalse_whenDifferentCreatedAt() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0, 0));

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных updatedAt")
    void equals_shouldReturnFalse_whenDifferentUpdatedAt() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setUpdatedAt(LocalDateTime.of(2024, 1, 16, 10, 0, 0));

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных lastLogin")
    void equals_shouldReturnFalse_whenDifferentLastLogin() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setLastLogin(LocalDateTime.of(2024, 1, 21, 15, 0, 0));

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false при разных trackedProducts")
    void equals_shouldReturnFalse_whenDifferentTrackedProducts() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setTrackedProducts(List.of(product1));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setTrackedProducts(List.of(product2));

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает true когда все поля null")
    void equals_shouldReturnTrue_whenAllFieldsNull() {
      User user1 = new User();
      User user2 = new User();

      assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false когда одно из полей null")
    void equals_shouldReturnFalse_whenOneFieldNullAndOtherNotNull() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");
      user1.setEmail("john@test.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername(null);
      user2.setEmail("john@test.com");

      assertThat(user1).isNotEqualTo(user2);
    }
  }

  // ==================== ДОПОЛНИТЕЛЬНЫЕ ТЕСТЫ ДЛЯ hashCode ====================

  @Nested
  @DisplayName("Дополнительные тесты метода hashCode")
  class HashCodeAdditionalTests {

    @Test
    @DisplayName("[УСПЕХ] hashCode одинаков для объектов с одинаковыми значениями полей")
    void hashCode_shouldBeEqual_whenAllFieldsEqual() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");
      user1.setEmail("john@example.com");
      user1.setFullName("John Doe");
      user1.setPasswordHash("hash");
      user1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
      user1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
      user1.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));
      user1.setTrackedProducts(List.of(product1));

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john_doe");
      user2.setEmail("john@example.com");
      user2.setFullName("John Doe");
      user2.setPasswordHash("hash");
      user2.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));
      user2.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0));
      user2.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));
      user2.setTrackedProducts(List.of(product1));

      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными id")
    void hashCode_shouldBeDifferent_whenDifferentId() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");

      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("john_doe");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными username")
    void hashCode_shouldBeDifferent_whenDifferentUsername() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john_doe");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("jane_doe");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными email")
    void hashCode_shouldBeDifferent_whenDifferentEmail() {
      User user1 = new User();
      user1.setId(1L);
      user1.setEmail("john@test.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setEmail("jane@test.com");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными fullName")
    void hashCode_shouldBeDifferent_whenDifferentFullName() {
      User user1 = new User();
      user1.setId(1L);
      user1.setFullName("John Doe");

      User user2 = new User();
      user2.setId(1L);
      user2.setFullName("Jane Doe");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными passwordHash")
    void hashCode_shouldBeDifferent_whenDifferentPasswordHash() {
      User user1 = new User();
      user1.setId(1L);
      user1.setPasswordHash("hash1");

      User user2 = new User();
      user2.setId(1L);
      user2.setPasswordHash("hash2");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными createdAt")
    void hashCode_shouldBeDifferent_whenDifferentCreatedAt() {
      User user1 = new User();
      user1.setId(1L);
      user1.setCreatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0, 0));

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными updatedAt")
    void hashCode_shouldBeDifferent_whenDifferentUpdatedAt() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUpdatedAt(LocalDateTime.of(2024, 1, 15, 10, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setUpdatedAt(LocalDateTime.of(2024, 1, 16, 10, 0, 0));

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными lastLogin")
    void hashCode_shouldBeDifferent_whenDifferentLastLogin() {
      User user1 = new User();
      user1.setId(1L);
      user1.setLastLogin(LocalDateTime.of(2024, 1, 20, 15, 0, 0));

      User user2 = new User();
      user2.setId(1L);
      user2.setLastLogin(LocalDateTime.of(2024, 1, 21, 15, 0, 0));

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для объектов с разными trackedProducts")
    void hashCode_shouldBeDifferent_whenDifferentTrackedProducts() {
      User user1 = new User();
      user1.setId(1L);
      user1.setTrackedProducts(List.of(product1));

      User user2 = new User();
      user2.setId(1L);
      user2.setTrackedProducts(List.of(product2));

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode одинаков для объектов с null полями")
    void hashCode_shouldBeEqual_whenAllFieldsNull() {
      User user1 = new User();
      User user2 = new User();

      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
  }

  // ==================== ТЕСТЫ НА СООТВЕТСТВИЕ equals И hashCode ====================

  @Nested
  @DisplayName("Тесты на соответствие equals и hashCode контракту")
  class EqualsHashCodeContractTests {

    @Test
    @DisplayName("[КОНТРАКТ] Если equals true, то hashCode должен быть одинаковым")
    void hashCode_shouldBeEqual_whenEqualsTrue() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");
      user1.setEmail("john@test.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john");
      user2.setEmail("john@test.com");

      assertThat(user1).isEqualTo(user2);
      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[КОНТРАКТ] Если equals false, hashCode может быть разным")
    void hashCode_mayBeDifferent_whenEqualsFalse() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");

      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("jane");

      assertThat(user1).isNotEqualTo(user2);
      // hashCode может быть одинаковым, но в данном случае они разные
      // Этот тест просто демонстрирует, что контракт не нарушен
    }

    @Test
    @DisplayName("[КОНТРАКТ] Многократный вызов hashCode возвращает одинаковое значение")
    void hashCode_shouldBeConsistent() {
      User testUser = new User();
      testUser.setId(1L);
      testUser.setUsername("john");

      int firstHash = testUser.hashCode();
      int secondHash = testUser.hashCode();
      int thirdHash = testUser.hashCode();

      assertThat(firstHash).isEqualTo(secondHash);
      assertThat(secondHash).isEqualTo(thirdHash);
    }
  }
}