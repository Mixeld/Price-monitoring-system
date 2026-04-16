package com.pricetracker.mapper;

import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для UserMapper")
class UserMapperTest {

  private UserMapper userMapper;

  private User existingUser;
  private UserDto userDto;
  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    userMapper = new UserMapper();

    product1 = new Product();
    product1.setId(101L);
    product1.setName("iPhone 15");

    product2 = new Product();
    product2.setId(102L);
    product2.setName("MacBook Pro");

    existingUser = new User();
    existingUser.setId(1L);
    existingUser.setUsername("john_doe");
    existingUser.setEmail("john@example.com");
    existingUser.setFullName("John Doe");
    existingUser.setPasswordHash("hashed_password");
    existingUser.setTrackedProducts(new ArrayList<>(List.of(product1, product2)));
    existingUser.setCreatedAt(LocalDateTime.now());
    existingUser.setUpdatedAt(LocalDateTime.now());

    userDto = new UserDto(1L, "john_doe", "john@example.com", "John Doe", List.of(101L, 102L));
  }

  // ==================== ТЕСТЫ ДЛЯ toDto ====================

  @Nested
  @DisplayName("Тесты метода toDto(User)")
  class ToDtoTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с трекаемыми продуктами")
    void toDto_shouldConvertUserToDto_withTrackedProducts() {
      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.username()).isEqualTo("john_doe");
      assertThat(result.email()).isEqualTo("john@example.com");
      assertThat(result.fullName()).isEqualTo("John Doe");
      assertThat(result.trackedProductIds()).containsExactly(101L, 102L);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с пустым списком трекаемых продуктов")
    void toDto_shouldConvertUserToDto_withEmptyTrackedProducts() {
      existingUser.setTrackedProducts(new ArrayList<>());

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.username()).isEqualTo("john_doe");
      assertThat(result.email()).isEqualTo("john@example.com");
      assertThat(result.fullName()).isEqualTo("John Doe");
      assertThat(result.trackedProductIds()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с null списком трекаемых продуктов")
    void toDto_shouldConvertUserToDto_withNullTrackedProducts() {
      existingUser.setTrackedProducts(null);

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.username()).isEqualTo("john_doe");
      assertThat(result.email()).isEqualTo("john@example.com");
      assertThat(result.fullName()).isEqualTo("John Doe");
      assertThat(result.trackedProductIds()).isEmpty(); // Должен вернуть пустой список, а не null
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с null id")
    void toDto_shouldConvertUserWithNullId() {
      existingUser.setId(null);

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.username()).isEqualTo("john_doe");
      assertThat(result.email()).isEqualTo("john@example.com");
      assertThat(result.fullName()).isEqualTo("John Doe");
      assertThat(result.trackedProductIds()).containsExactly(101L, 102L);
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с null username")
    void toDto_shouldConvertUserWithNullUsername() {
      existingUser.setUsername(null);

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(1L);
      assertThat(result.username()).isNull();
      assertThat(result.email()).isEqualTo("john@example.com");
      assertThat(result.fullName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с пустым username")
    void toDto_shouldConvertUserWithEmptyUsername() {
      existingUser.setUsername("");

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.username()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с null email")
    void toDto_shouldConvertUserWithNullEmail() {
      existingUser.setEmail(null);

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.email()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с пустым email")
    void toDto_shouldConvertUserWithEmptyEmail() {
      existingUser.setEmail("");

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.email()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с null fullName")
    void toDto_shouldConvertUserWithNullFullName() {
      existingUser.setFullName(null);

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.fullName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация User в UserDto с пустым fullName")
    void toDto_shouldConvertUserWithEmptyFullName() {
      existingUser.setFullName("");

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.fullName()).isEmpty();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null User в null DTO")
    void toDto_shouldReturnNull_whenUserIsNull() {
      UserDto result = userMapper.toDto(null);

      assertThat(result).isNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация User с одним трекаемым продуктом")
    void toDto_shouldConvertUserWithSingleTrackedProduct() {
      existingUser.setTrackedProducts(List.of(product1));

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.trackedProductIds()).containsExactly(101L);
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация User с трекаемыми продуктами, у которых null id")
    void toDto_shouldHandleProductsWithNullId() {
      Product productWithNullId = new Product();
      productWithNullId.setId(null);
      productWithNullId.setName("Product with null ID");
      existingUser.setTrackedProducts(List.of(productWithNullId));

      UserDto result = userMapper.toDto(existingUser);

      assertThat(result).isNotNull();
      assertThat(result.trackedProductIds()).containsNull();
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация User со всеми null полями")
    void toDto_shouldHandleAllNullFields() {
      User user = new User();
      user.setId(null);
      user.setUsername(null);
      user.setEmail(null);
      user.setFullName(null);
      user.setTrackedProducts(null);

      UserDto result = userMapper.toDto(user);

      assertThat(result).isNotNull();
      assertThat(result.id()).isNull();
      assertThat(result.username()).isNull();
      assertThat(result.email()).isNull();
      assertThat(result.fullName()).isNull();
      assertThat(result.trackedProductIds()).isEmpty();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ toEntity ====================

  @Nested
  @DisplayName("Тесты метода toEntity(UserDto)")
  class ToEntityTests {

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto в User")
    void toEntity_shouldConvertDtoToUser() {
      User result = userMapper.toEntity(userDto);

      assertThat(result).isNotNull();
      // ID копируется из DTO в Entity согласно реализации маппера
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getUsername()).isEqualTo("john_doe");
      assertThat(result.getEmail()).isEqualTo("john@example.com");
      assertThat(result.getFullName()).isEqualTo("John Doe");
      // trackedProducts инициализируется пустым списком (не null) в конструкторе/полях Entity
      assertThat(result.getTrackedProducts()).isNotNull();
      assertThat(result.getTrackedProducts()).isEmpty();
      // passwordHash не устанавливается
      assertThat(result.getPasswordHash()).isNull();
      // временные метки не устанавливаются
      assertThat(result.getCreatedAt()).isNull();
      assertThat(result.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с null id")
    void toEntity_shouldConvertDtoWithNullId() {
      UserDto dto = new UserDto(null, "jane_doe", "jane@example.com", "Jane Doe", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isNull();
      assertThat(result.getUsername()).isEqualTo("jane_doe");
      assertThat(result.getEmail()).isEqualTo("jane@example.com");
      assertThat(result.getFullName()).isEqualTo("Jane Doe");
      assertThat(result.getTrackedProducts()).isNotNull();
      assertThat(result.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с null username")
    void toEntity_shouldConvertDtoWithNullUsername() {
      UserDto dto = new UserDto(1L, null, "john@example.com", "John Doe", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с пустым username")
    void toEntity_shouldConvertDtoWithEmptyUsername() {
      UserDto dto = new UserDto(1L, "", "john@example.com", "John Doe", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с null email")
    void toEntity_shouldConvertDtoWithNullEmail() {
      UserDto dto = new UserDto(1L, "john_doe", null, "John Doe", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с пустым email")
    void toEntity_shouldConvertDtoWithEmptyEmail() {
      UserDto dto = new UserDto(1L, "john_doe", "", "John Doe", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с null fullName")
    void toEntity_shouldConvertDtoWithNullFullName() {
      UserDto dto = new UserDto(1L, "john_doe", "john@example.com", null, List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getFullName()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с пустым fullName")
    void toEntity_shouldConvertDtoWithEmptyFullName() {
      UserDto dto = new UserDto(1L, "john_doe", "john@example.com", "", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getFullName()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с null trackedProductIds (игнорируется)")
    void toEntity_shouldIgnoreTrackedProductIds() {
      UserDto dto = new UserDto(1L, "john_doe", "john@example.com", "John Doe", null);

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      // trackedProducts инициализируется пустым списком в Entity
      assertThat(result.getTrackedProducts()).isNotNull();
      assertThat(result.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с пустым списком trackedProductIds (игнорируется)")
    void toEntity_shouldIgnoreEmptyTrackedProductIds() {
      UserDto dto = new UserDto(1L, "john_doe", "john@example.com", "John Doe", Collections.emptyList());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getTrackedProducts()).isNotNull();
      assertThat(result.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Конвертация UserDto с id = 0")
    void toEntity_shouldConvertDtoWithZeroId() {
      UserDto dto = new UserDto(0L, "user_zero", "zero@example.com", "User Zero", List.of());

      User result = userMapper.toEntity(dto);

      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(0L);
      assertThat(result.getUsername()).isEqualTo("user_zero");
    }

    @Test
    @DisplayName("[ГРАНИЦЫ] Конвертация null DTO в null Entity")
    void toEntity_shouldReturnNull_whenDtoIsNull() {
      User result = userMapper.toEntity(null);

      assertThat(result).isNull();
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (конвертация туда и обратно)")
  class RoundTripTests {

    @Test
    @DisplayName("[УСПЕХ] Round-trip конвертация: User -> UserDto -> User")
    void roundTrip_shouldPreserveData() {
      // User -> UserDto
      UserDto dto = userMapper.toDto(existingUser);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isEqualTo(1L);
      assertThat(dto.username()).isEqualTo("john_doe");
      assertThat(dto.email()).isEqualTo("john@example.com");
      assertThat(dto.fullName()).isEqualTo("John Doe");
      assertThat(dto.trackedProductIds()).containsExactly(101L, 102L);

      // UserDto -> User
      User userBack = userMapper.toEntity(dto);

      assertThat(userBack).isNotNull();
      // ID восстанавливается (копируется из DTO)
      assertThat(userBack.getId()).isEqualTo(1L);
      assertThat(userBack.getUsername()).isEqualTo("john_doe");
      assertThat(userBack.getEmail()).isEqualTo("john@example.com");
      assertThat(userBack.getFullName()).isEqualTo("John Doe");
      // trackedProducts инициализируется пустым списком (не null)
      assertThat(userBack.getTrackedProducts()).isNotNull();
      assertThat(userBack.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Создание нового User через DTO")
    void roundTrip_shouldCreateNewUser() {
      UserDto newUserDto = new UserDto(null, "new_user", "new@example.com", "New User", List.of());

      User entity = userMapper.toEntity(newUserDto);

      assertThat(entity.getId()).isNull();
      assertThat(entity.getUsername()).isEqualTo("new_user");
      assertThat(entity.getEmail()).isEqualTo("new@example.com");
      assertThat(entity.getFullName()).isEqualTo("New User");
      assertThat(entity.getTrackedProducts()).isNotNull();
      assertThat(entity.getTrackedProducts()).isEmpty();
    }

    @Test
    @DisplayName("[УСПЕХ] Round-trip с null значениями")
    void roundTrip_shouldHandleNullValues() {
      User user = new User();
      user.setId(null);
      user.setUsername(null);
      user.setEmail(null);
      user.setFullName(null);
      user.setTrackedProducts(null);

      // User -> UserDto
      UserDto dto = userMapper.toDto(user);

      assertThat(dto).isNotNull();
      assertThat(dto.id()).isNull();
      assertThat(dto.username()).isNull();
      assertThat(dto.email()).isNull();
      assertThat(dto.fullName()).isNull();
      assertThat(dto.trackedProductIds()).isEmpty();

      // UserDto -> User
      User userBack = userMapper.toEntity(dto);

      assertThat(userBack).isNotNull();
      assertThat(userBack.getId()).isNull();
      assertThat(userBack.getUsername()).isNull();
      assertThat(userBack.getEmail()).isNull();
      assertThat(userBack.getFullName()).isNull();
      assertThat(userBack.getTrackedProducts()).isNotNull();
      assertThat(userBack.getTrackedProducts()).isEmpty();
    }
  }
}