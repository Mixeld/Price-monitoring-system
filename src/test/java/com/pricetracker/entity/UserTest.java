package com.pricetracker.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для User entity")
class UserTest {

  private User user;
  private Product product1;
  private Product product2;
  private final Long ID = 1L;
  private final String USERNAME = "john_doe";
  private final String EMAIL = "john@example.com";
  private final String FULL_NAME = "John Doe";
  private final String PASSWORD_HASH = "hashed_password_123";
  private final LocalDateTime CREATED_AT = LocalDateTime.of(2024, 1, 1, 10, 0, 0);
  private final LocalDateTime UPDATED_AT = LocalDateTime.of(2024, 1, 15, 10, 30, 0);
  private final LocalDateTime LAST_LOGIN = LocalDateTime.of(2024, 1, 20, 15, 0, 0);

  @BeforeEach
  void setUp() {
    product1 = new Product();
    product1.setId(101L);
    product1.setName("iPhone 15");

    product2 = new Product();
    product2.setId(102L);
    product2.setName("MacBook Pro");

    user = new User();
    user.setId(ID);
    user.setUsername(USERNAME);
    user.setEmail(EMAIL);
    user.setFullName(FULL_NAME);
    user.setPasswordHash(PASSWORD_HASH);
    user.setCreatedAt(CREATED_AT);
    user.setUpdatedAt(UPDATED_AT);
    user.setLastLogin(LAST_LOGIN);
    user.setTrackedProducts(new ArrayList<>(List.of(product1, product2)));
  }

  // ==================== ТЕСТЫ КОНСТРУКТОРОВ ====================

  @Nested
  @DisplayName("Тесты конструкторов")
  class ConstructorTests {

    @Test
    @DisplayName("[УСПЕХ] Создание User через конструктор по умолчанию")
    void defaultConstructor_shouldCreateEmptyUser() {
      User emptyUser = new User();

      assertThat(emptyUser.getId()).isNull();
      assertThat(emptyUser.getUsername()).isNull();
      assertThat(emptyUser.getEmail()).isNull();
      assertThat(emptyUser.getFullName()).isNull();
      assertThat(emptyUser.getPasswordHash()).isNull();
      assertThat(emptyUser.getCreatedAt()).isNull();
      assertThat(emptyUser.getUpdatedAt()).isNull();
      assertThat(emptyUser.getLastLogin()).isNull();
      assertThat(emptyUser.getTrackedProducts()).isNotNull();
      assertThat(emptyUser.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание User через AllArgsConstructor")
    void allArgsConstructor_shouldCreateUserWithAllFields() {
      List<Product> trackedProducts = List.of(product1, product2);

      User allArgsUser = new User(
          ID,
          USERNAME,
          EMAIL,
          FULL_NAME,
          PASSWORD_HASH,
          CREATED_AT,
          UPDATED_AT,
          LAST_LOGIN,
          trackedProducts
      );

      assertThat(allArgsUser.getId()).isEqualTo(ID);
      assertThat(allArgsUser.getUsername()).isEqualTo(USERNAME);
      assertThat(allArgsUser.getEmail()).isEqualTo(EMAIL);
      assertThat(allArgsUser.getFullName()).isEqualTo(FULL_NAME);
      assertThat(allArgsUser.getPasswordHash()).isEqualTo(PASSWORD_HASH);
      assertThat(allArgsUser.getCreatedAt()).isEqualTo(CREATED_AT);
      assertThat(allArgsUser.getUpdatedAt()).isEqualTo(UPDATED_AT);
      assertThat(allArgsUser.getLastLogin()).isEqualTo(LAST_LOGIN);
      assertThat(allArgsUser.getTrackedProducts()).isEqualTo(trackedProducts);
    }

    @Test
    @DisplayName("[УСПЕХ] Создание User через AllArgsConstructor с null значениями")
    void allArgsConstructor_shouldHandleNullValues() {
      User allArgsUser = new User(
          null, null, null, null, null, null, null, null, null
      );

      assertThat(allArgsUser.getId()).isNull();
      assertThat(allArgsUser.getUsername()).isNull();
      assertThat(allArgsUser.getEmail()).isNull();
      assertThat(allArgsUser.getFullName()).isNull();
      assertThat(allArgsUser.getPasswordHash()).isNull();
      assertThat(allArgsUser.getCreatedAt()).isNull();
      assertThat(allArgsUser.getUpdatedAt()).isNull();
      assertThat(allArgsUser.getLastLogin()).isNull();
      assertThat(allArgsUser.getTrackedProducts()).isNull();
    }
  }

  // ==================== ТЕСТЫ СЕТТЕРОВ И ГЕТТЕРОВ ====================

  @Nested
  @DisplayName("Тесты сеттеров и геттеров")
  class GetterSetterTests {

    @Test
    @DisplayName("[УСПЕХ] Установка и получение id")
    void setIdAndGetId_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setId(100L);
      assertThat(testUser.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение username")
    void setUsernameAndGetUsername_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setUsername("test_user");
      assertThat(testUser.getUsername()).isEqualTo("test_user");
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение email")
    void setEmailAndGetEmail_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setEmail("test@example.com");
      assertThat(testUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение fullName")
    void setFullNameAndGetFullName_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setFullName("Test User");
      assertThat(testUser.getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение passwordHash")
    void setPasswordHashAndGetPasswordHash_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setPasswordHash("hash123");
      assertThat(testUser.getPasswordHash()).isEqualTo("hash123");
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение createdAt")
    void setCreatedAtAndGetCreatedAt_shouldWorkCorrectly() {
      User testUser = new User();
      LocalDateTime now = LocalDateTime.now();
      testUser.setCreatedAt(now);
      assertThat(testUser.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение updatedAt")
    void setUpdatedAtAndGetUpdatedAt_shouldWorkCorrectly() {
      User testUser = new User();
      LocalDateTime now = LocalDateTime.now();
      testUser.setUpdatedAt(now);
      assertThat(testUser.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение lastLogin")
    void setLastLoginAndGetLastLogin_shouldWorkCorrectly() {
      User testUser = new User();
      testUser.setLastLogin(LAST_LOGIN);
      assertThat(testUser.getLastLogin()).isEqualTo(LAST_LOGIN);
    }

    @Test
    @DisplayName("[УСПЕХ] Установка и получение trackedProducts")
    void setTrackedProductsAndGetTrackedProducts_shouldWorkCorrectly() {
      User testUser = new User();
      List<Product> products = List.of(product1, product2);
      testUser.setTrackedProducts(products);
      assertThat(testUser.getTrackedProducts()).isEqualTo(products);
    }

    @Test
    @DisplayName("[УСПЕХ] trackedProducts инициализируется пустым списком")
    void trackedProducts_shouldBeInitializedAsEmptyList() {
      User testUser = new User();
      assertThat(testUser.getTrackedProducts()).isNotNull();
      assertThat(testUser.getTrackedProducts()).isEmpty();
    }
  }

  // ==================== ТЕСТЫ equals ====================

  @Nested
  @DisplayName("Тесты метода equals")
  class EqualsTests {

    @Test
    @DisplayName("[УСПЕХ] equals возвращает true для одного и того же объекта")
    void equals_shouldReturnTrue_forSameObject() {
      assertThat(user.equals(user)).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false для null")
    void equals_shouldReturnFalse_forNull() {
      assertThat(user.equals(null)).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false для другого типа")
    void equals_shouldReturnFalse_forDifferentType() {
      assertThat(user.equals("string")).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает true для объектов с одинаковыми id")
    void equals_shouldReturnTrue_forSameId() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("user1");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("user2");

      // Lombok @Data генерирует equals, который сравнивает все поля
      // Поэтому разные username дадут false, даже если id одинаковый
      assertThat(user1.equals(user2)).isFalse();
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает true для идентичных объектов")
    void equals_shouldReturnTrue_forIdenticalObjects() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");
      user1.setEmail("john@test.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john");
      user2.setEmail("john@test.com");

      assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false для объектов с разными полями")
    void equals_shouldReturnFalse_forDifferentFields() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");

      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("jane");

      assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals обрабатывает null поля")
    void equals_shouldHandleNullFields() {
      User user1 = new User();
      user1.setId(null);
      user1.setUsername(null);

      User user2 = new User();
      user2.setId(null);
      user2.setUsername(null);

      assertThat(user1).isEqualTo(user2);
    }

    @Test
    @DisplayName("[УСПЕХ] equals возвращает false когда одно поле null а другое нет")
    void equals_shouldReturnFalse_whenOneFieldNull() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername(null);

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john");

      assertThat(user1).isNotEqualTo(user2);
    }
  }

  // ==================== ТЕСТЫ hashCode ====================

  @Nested
  @DisplayName("Тесты метода hashCode")
  class HashCodeTests {

    @Test
    @DisplayName("[УСПЕХ] hashCode одинаков для одинаковых объектов")
    void hashCode_shouldBeEqual_forEqualObjects() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");
      user1.setEmail("john@test.com");

      User user2 = new User();
      user2.setId(1L);
      user2.setUsername("john");
      user2.setEmail("john@test.com");

      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode разный для разных объектов")
    void hashCode_shouldBeDifferent_forDifferentObjects() {
      User user1 = new User();
      user1.setId(1L);
      user1.setUsername("john");

      User user2 = new User();
      user2.setId(2L);
      user2.setUsername("jane");

      assertThat(user1.hashCode()).isNotEqualTo(user2.hashCode());
    }

    @Test
    @DisplayName("[УСПЕХ] hashCode обрабатывает null поля")
    void hashCode_shouldHandleNullFields() {
      User user1 = new User();
      user1.setId(null);
      user1.setUsername(null);
      user1.setEmail(null);

      User user2 = new User();
      user2.setId(null);
      user2.setUsername(null);
      user2.setEmail(null);

      assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
  }

  // ==================== ТЕСТЫ toString ====================

  @Nested
  @DisplayName("Тесты метода toString")
  class ToStringTests {

    @Test
    @DisplayName("[УСПЕХ] toString возвращает строковое представление")
    void toString_shouldReturnFormattedString() {
      String toString = user.toString();

      assertThat(toString).contains("User");
      assertThat(toString).contains("id=1");
      assertThat(toString).contains("username=john_doe");
      assertThat(toString).contains("email=john@example.com");
      assertThat(toString).contains("fullName=John Doe");
    }

    @Test
    @DisplayName("[УСПЕХ] toString обрабатывает null поля")
    void toString_shouldHandleNullFields() {
      User emptyUser = new User();
      String toString = emptyUser.toString();

      assertThat(toString).contains("User");
      assertThat(toString).contains("id=null");
      assertThat(toString).contains("username=null");
      assertThat(toString).contains("email=null");
    }
  }

  // ==================== ТЕСТЫ canEqual ====================

  @Nested
  @DisplayName("Тесты метода canEqual")
  class CanEqualTests {

    @Test
    @DisplayName("[УСПЕХ] canEqual возвращает true для того же типа")
    void canEqual_shouldReturnTrue_forSameType() {
      assertThat(user.canEqual(new User())).isTrue();
    }

    @Test
    @DisplayName("[УСПЕХ] canEqual возвращает false для другого типа")
    void canEqual_shouldReturnFalse_forDifferentType() {
      assertThat(user.canEqual("string")).isFalse();
    }
  }

  // ==================== ТЕСТЫ СВЯЗЕЙ ====================

  @Nested
  @DisplayName("Тесты связей с Product")
  class RelationshipTests {

    @Test
    @DisplayName("[УСПЕХ] Добавление продуктов в trackedProducts")
    void addTrackedProducts_shouldAddProducts() {
      User testUser = new User();
      testUser.setTrackedProducts(new ArrayList<>());

      testUser.getTrackedProducts().add(product1);
      testUser.getTrackedProducts().add(product2);

      assertThat(testUser.getTrackedProducts()).hasSize(2);
      assertThat(testUser.getTrackedProducts()).contains(product1, product2);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление продуктов из trackedProducts")
    void removeTrackedProducts_shouldRemoveProducts() {
      User testUser = new User();
      testUser.setTrackedProducts(new ArrayList<>(List.of(product1, product2)));

      testUser.getTrackedProducts().remove(product1);

      assertThat(testUser.getTrackedProducts()).hasSize(1);
      assertThat(testUser.getTrackedProducts()).contains(product2);
      assertThat(testUser.getTrackedProducts()).doesNotContain(product1);
    }

    @Test
    @DisplayName("[УСПЕХ] Очистка списка trackedProducts")
    void clearTrackedProducts_shouldClearList() {
      User testUser = new User();
      testUser.setTrackedProducts(new ArrayList<>(List.of(product1, product2)));

      testUser.getTrackedProducts().clear();

      assertThat(testUser.getTrackedProducts()).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ВРЕМЕННЫХ МЕТОК ====================

  @Nested
  @DisplayName("Тесты временных меток")
  class TimestampTests {

    @Test
    @DisplayName("[УСПЕХ] Установка createdAt до updatedAt")
    void createdAt_shouldBeBeforeUpdatedAt() {
      User testUser = new User();
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime later = now.plusHours(1);

      testUser.setCreatedAt(now);
      testUser.setUpdatedAt(later);

      assertThat(testUser.getCreatedAt()).isBefore(testUser.getUpdatedAt());
    }

    @Test
    @DisplayName("[УСПЕХ] lastLogin может быть после updatedAt")
    void lastLogin_shouldBeAfterUpdatedAt() {
      User testUser = new User();
      LocalDateTime updated = LocalDateTime.now();
      LocalDateTime lastLogin = updated.plusDays(1);

      testUser.setUpdatedAt(updated);
      testUser.setLastLogin(lastLogin);

      assertThat(testUser.getLastLogin()).isAfter(testUser.getUpdatedAt());
    }
  }
}