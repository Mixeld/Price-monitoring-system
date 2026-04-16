package com.pricetracker.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Unit-тесты для UserCreateDto")
class UserCreateDtoTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  // ==================== ТЕСТЫ КОНСТРУКТОРА ====================

  @Test
  @DisplayName("[УСПЕХ] Создание DTO через канонический конструктор")
  void constructor_shouldCreateValidDto() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");

    assertThat(dto.username()).isEqualTo("john_doe");
    assertThat(dto.email()).isEqualTo("john@example.com");
    assertThat(dto.fullName()).isEqualTo("John Doe");
    assertThat(dto.password()).isEqualTo("password123");
  }

  @Test
  @DisplayName("[УСПЕХ] Создание DTO с минимальными допустимыми значениями")
  void constructor_shouldCreateDtoWithMinimalValues() {
    UserCreateDto dto = new UserCreateDto("usr", "a@b.c", "Jo", "123456");

    assertThat(dto.username()).hasSize(3);
    assertThat(dto.email()).isEqualTo("a@b.c");
    assertThat(dto.fullName()).hasSize(2);
    assertThat(dto.password()).hasSize(6);
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - USERNAME ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректный username")
  void validation_shouldPass_whenUsernameIsValid() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - username равен null")
  void validation_shouldFail_whenUsernameIsNull() {
    UserCreateDto dto = new UserCreateDto(null, "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Username is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - username пустая строка (две ошибки)")
  void validation_shouldFail_whenUsernameIsEmpty() {
    UserCreateDto dto = new UserCreateDto("", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(2);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Username is required",
        "Username must be between 3 and 50 characters"
    );
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - username короче 3 символов")
  void validation_shouldFail_whenUsernameIsTooShort() {
    UserCreateDto dto = new UserCreateDto("us", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 50 characters");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - username максимальной длины (50 символов)")
  void validation_shouldPass_whenUsernameHasMaxLength() {
    String username = "a".repeat(50);
    UserCreateDto dto = new UserCreateDto(username, "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - username длиннее 50 символов")
  void validation_shouldFail_whenUsernameIsTooLong() {
    String username = "a".repeat(51);
    UserCreateDto dto = new UserCreateDto(username, "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Username must be between 3 and 50 characters");
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - EMAIL ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректный email")
  void validation_shouldPass_whenEmailIsValid() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - email с поддоменом")
  void validation_shouldPass_whenEmailHasSubdomain() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@mail.example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - email с плюсом")
  void validation_shouldPass_whenEmailHasPlus() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john+test@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - email равен null")
  void validation_shouldFail_whenEmailIsNull() {
    UserCreateDto dto = new UserCreateDto("john_doe", null, "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - email пустая строка (только @NotBlank, т.к. пустая строка не проходит валидацию email формата)")
  void validation_shouldFail_whenEmailIsEmpty() {
    UserCreateDto dto = new UserCreateDto("john_doe", "", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Email is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - email без @ (только @Email)")
  void validation_shouldFail_whenEmailMissingAtSymbol() {
    UserCreateDto dto = new UserCreateDto("john_doe", "johnexample.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - email без домена")
  void validation_shouldFail_whenEmailMissingDomain() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - email без локальной части")
  void validation_shouldFail_whenEmailMissingLocalPart() {
    UserCreateDto dto = new UserCreateDto("john_doe", "@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Invalid email format");
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - FULLNAME ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректный fullName")
  void validation_shouldPass_whenFullNameIsValid() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - fullName с пробелами и спецсимволами")
  void validation_shouldPass_whenFullNameHasSpacesAndSpecialChars() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John M. Doe Jr.", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - fullName равен null")
  void validation_shouldFail_whenFullNameIsNull() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", null, "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Full name is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - fullName пустая строка (две ошибки)")
  void validation_shouldFail_whenFullNameIsEmpty() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(2);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Full name is required",
        "Full name must be between 2 and 100 characters"
    );
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - fullName короче 2 символов")
  void validation_shouldFail_whenFullNameIsTooShort() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "J", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Full name must be between 2 and 100 characters");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - fullName максимальной длины (100 символов)")
  void validation_shouldPass_whenFullNameHasMaxLength() {
    String fullName = "A".repeat(100);
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", fullName, "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - fullName длиннее 100 символов")
  void validation_shouldFail_whenFullNameIsTooLong() {
    String fullName = "A".repeat(101);
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", fullName, "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Full name must be between 2 and 100 characters");
  }

  // ==================== ТЕСТЫ ВАЛИДАЦИИ - PASSWORD ====================

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - корректный password")
  void validation_shouldPass_whenPasswordIsValid() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - password со спецсимволами")
  void validation_shouldPass_whenPasswordHasSpecialChars() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "P@ssw0rd!@#$%");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - password равен null")
  void validation_shouldFail_whenPasswordIsNull() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", null);
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Password is required");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - password пустая строка (две ошибки)")
  void validation_shouldFail_whenPasswordIsEmpty() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(2);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Password is required",
        "Password must be at least 6 characters"
    );
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Ошибка - password короче 6 символов")
  void validation_shouldFail_whenPasswordIsTooShort() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "12345");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage()).isEqualTo("Password must be at least 6 characters");
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - password минимальной длины (6 символов)")
  void validation_shouldPass_whenPasswordHasMinLength() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "123456");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ВАЛИДАЦИЯ] Успех - password очень длинный")
  void validation_shouldPass_whenPasswordIsVeryLong() {
    String password = "a".repeat(1000);
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", password);
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  // ==================== КОМБИНИРОВАННЫЕ ТЕСТЫ ====================

  @Test
  @DisplayName("[КОМБИНАЦИИ] Множественные ошибки валидации")
  void validation_shouldCollectMultipleViolations() {
    UserCreateDto dto = new UserCreateDto("us", "invalid-email", "J", "123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(4);

    Set<String> messages = violations.stream()
        .map(ConstraintViolation::getMessage)
        .collect(Collectors.toSet());

    assertThat(messages).contains(
        "Username must be between 3 and 50 characters",
        "Invalid email format",
        "Full name must be between 2 and 100 characters",
        "Password must be at least 6 characters"
    );
  }

  @Test
  @DisplayName("[КОМБИНАЦИИ] Все поля некорректны")
  void validation_shouldFail_whenAllFieldsAreInvalid() {
    UserCreateDto dto = new UserCreateDto("", "", "", "");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);

    assertThat(violations).hasSizeGreaterThanOrEqualTo(4);

    boolean hasUsernameError = violations.stream()
        .anyMatch(v -> v.getMessage().contains("Username"));
    boolean hasEmailError = violations.stream()
        .anyMatch(v -> v.getMessage().contains("Email"));
    boolean hasFullNameError = violations.stream()
        .anyMatch(v -> v.getMessage().contains("Full name"));
    boolean hasPasswordError = violations.stream()
        .anyMatch(v -> v.getMessage().contains("Password"));

    assertThat(hasUsernameError).isTrue();
    assertThat(hasEmailError).isTrue();
    assertThat(hasFullNameError).isTrue();
    assertThat(hasPasswordError).isTrue();
  }

  // ==================== ТЕСТЫ МЕТОДОВ RECORD ====================

  @Test
  @DisplayName("[УСПЕХ] Record методы equals и hashCode")
  void record_shouldImplementEqualsAndHashCodeCorrectly() {
    UserCreateDto dto1 = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    UserCreateDto dto2 = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    UserCreateDto dto3 = new UserCreateDto("jane_doe", "jane@example.com", "Jane Doe", "password456");

    assertThat(dto1).isEqualTo(dto2);
    assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    assertThat(dto1).isNotEqualTo(dto3);
  }

  @Test
  @DisplayName("[УСПЕХ] Record метод toString")
  void record_shouldImplementToString() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "secret123");
    String toString = dto.toString();

    assertThat(toString).contains("UserCreateDto");
    assertThat(toString).contains("john_doe");
    assertThat(toString).contains("john@example.com");
    assertThat(toString).contains("John Doe");
    assertThat(toString).contains("secret123");
  }

  // ==================== ГРАНИЧНЫЕ ТЕСТЫ ====================

  @Test
  @DisplayName("[ГРАНИЦЫ] Username из 3 символов - минимальная граница")
  void boundary_usernameWithMinLength_shouldPass() {
    UserCreateDto dto = new UserCreateDto("abc", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Username из 2 символов - ниже минимальной границы")
  void boundary_usernameBelowMinLength_shouldFail() {
    UserCreateDto dto = new UserCreateDto("ab", "john@example.com", "John Doe", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] FullName из 2 символов - минимальная граница")
  void boundary_fullNameWithMinLength_shouldPass() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "Jo", "password123");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Password из 6 символов - минимальная граница")
  void boundary_passwordWithMinLength_shouldPass() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "123456");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).isEmpty();
  }

  @Test
  @DisplayName("[ГРАНИЦЫ] Password из 5 символов - ниже минимальной границы")
  void boundary_passwordBelowMinLength_shouldFail() {
    UserCreateDto dto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "12345");
    Set<ConstraintViolation<UserCreateDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(1);
  }
}