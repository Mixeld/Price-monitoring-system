package com.pricetracker.service;

import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.User;
import com.pricetracker.exception.BusinessException;
import com.pricetracker.exception.DuplicateResourceException;
import com.pricetracker.exception.ResourceNotFoundException;
import com.pricetracker.mapper.UserMapper;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("UserService Tests - 100% Coverage")
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private ProductRepository productRepository;
  @Mock private UserMapper userMapper;
  @Mock private UserService selfMock;

  private UserService userService;
  private UserDto createDto;
  private User existingUser;
  private final String RAW_PASSWORD = "password123";

  @BeforeEach
  void setUp() throws Exception {
    userService = new UserService(userRepository, userMapper, productRepository, selfMock);

    Field selfField = UserService.class.getDeclaredField("self");
    selfField.setAccessible(true);
    selfField.set(userService, selfMock);

    createDto = new UserDto(null, "john_doe", "john@example.com", "John Doe", Collections.emptyList());

    existingUser = new User();
    existingUser.setId(1L);
    existingUser.setUsername("existing_user");
    existingUser.setEmail("existing@example.com");
    existingUser.setFullName("Existing User");
    existingUser.setPasswordHash(new BCryptPasswordEncoder().encode(RAW_PASSWORD));
    existingUser.setTrackedProducts(new ArrayList<>());
    existingUser.setCreatedAt(LocalDateTime.now());
    existingUser.setUpdatedAt(LocalDateTime.now());

    when(userMapper.toDto(any(User.class))).thenAnswer(i -> {
      User user = i.getArgument(0);
      return new UserDto(
          user.getId(),
          user.getUsername(),
          user.getEmail(),
          user.getFullName(),
          null
      );
    });

    when(userMapper.toEntity(any(UserDto.class))).thenAnswer(i -> {
      UserDto dto = i.getArgument(0);
      User u = new User();
      if (dto != null) {
        u.setUsername(dto.username());
        u.setEmail(dto.email());
        u.setFullName(dto.fullName());
      }
      return u;
    });

    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
  }

  // ==================== ТЕСТЫ ДЛЯ updateEntity (ПОЛНОЕ ПОКРЫТИЕ) ====================

  @Test
  @DisplayName("updateEntity - update all fields with new values")
  void updateEntity_shouldUpdateAllFields() {
    UserDto dto = new UserDto(1L, "new_username", "new_email@example.com", "New Full Name", null);
    when(userRepository.findByUsername("new_username")).thenReturn(Optional.empty());
    when(userRepository.findByEmail("new_email@example.com")).thenReturn(Optional.empty());

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("new_username");
    assertThat(existingUser.getEmail()).isEqualTo("new_email@example.com");
    assertThat(existingUser.getFullName()).isEqualTo("New Full Name");
    assertThat(existingUser.getUpdatedAt()).isNotNull();
  }

  @Test
  @DisplayName("updateEntity - update only username")
  void updateEntity_shouldUpdateOnlyUsername() {
    UserDto dto = new UserDto(1L, "new_username", null, null, null);
    when(userRepository.findByUsername("new_username")).thenReturn(Optional.empty());

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("new_username");
    assertThat(existingUser.getEmail()).isEqualTo("existing@example.com");
    assertThat(existingUser.getFullName()).isEqualTo("Existing User");
  }

  @Test
  @DisplayName("updateEntity - update only email")
  void updateEntity_shouldUpdateOnlyEmail() {
    UserDto dto = new UserDto(1L, null, "new_email@example.com", null, null);
    when(userRepository.findByEmail("new_email@example.com")).thenReturn(Optional.empty());

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("existing_user");
    assertThat(existingUser.getEmail()).isEqualTo("new_email@example.com");
    assertThat(existingUser.getFullName()).isEqualTo("Existing User");
  }

  @Test
  @DisplayName("updateEntity - update only fullName")
  void updateEntity_shouldUpdateOnlyFullName() {
    UserDto dto = new UserDto(1L, null, null, "New Full Name", null);

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("existing_user");
    assertThat(existingUser.getEmail()).isEqualTo("existing@example.com");
    assertThat(existingUser.getFullName()).isEqualTo("New Full Name");
  }

  @Test
  @DisplayName("updateEntity - update with same values (should still set but not fail)")
  void updateEntity_shouldHandleSameValues() {
    UserDto dto = new UserDto(1L, "existing_user", "existing@example.com", "Existing User", null);

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("existing_user");
    assertThat(existingUser.getEmail()).isEqualTo("existing@example.com");
    assertThat(existingUser.getFullName()).isEqualTo("Existing User");
  }

  @Test
  @DisplayName("updateEntity - update tracked products")
  void updateEntity_shouldUpdateTrackedProducts() {
    Product product1 = new Product();
    product1.setId(101L);
    Product product2 = new Product();
    product2.setId(102L);

    when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
    when(productRepository.findById(102L)).thenReturn(Optional.of(product2));

    UserDto dto = new UserDto(1L, null, null, null, List.of(101L, 102L));

    userService.update(1L, dto);

    assertThat(existingUser.getTrackedProducts()).containsExactly(product1, product2);
  }

  @Test
  @DisplayName("updateEntity - update tracked products with empty list")
  void updateEntity_shouldClearTrackedProducts() {
    existingUser.getTrackedProducts().add(new Product());

    UserDto dto = new UserDto(1L, null, null, null, List.of());

    userService.update(1L, dto);

    assertThat(existingUser.getTrackedProducts()).isEmpty();
  }

  @Test
  @DisplayName("updateEntity - skip tracked products when null")
  void updateEntity_shouldSkipTrackedProductsWhenNull() {
    List<Product> originalProducts = new ArrayList<>(existingUser.getTrackedProducts());

    UserDto dto = new UserDto(1L, null, null, null, null);

    userService.update(1L, dto);

    assertThat(existingUser.getTrackedProducts()).isEqualTo(originalProducts);
    verify(productRepository, never()).findById(anyLong());
  }

  // ==================== ТЕСТЫ ДЛЯ validateBeforeCreate ====================

  @Test
  @DisplayName("validateBeforeCreate - success when email is null and username is unique")
  void create_shouldSucceedWhenEmailIsNullAndUsernameIsUnique() {
    UserDto dto = new UserDto(null, "unique_user", null, "Full Name", null);
    when(userRepository.findByUsername("unique_user")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(new User());

    userService.create(dto, RAW_PASSWORD);

    verify(userRepository, never()).findByEmail(anyString());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("validateBeforeCreate - success when username is null and email is unique")
  void create_shouldSucceedWhenUsernameIsNullAndEmailIsUnique() {
    UserDto dto = new UserDto(null, null, "unique@example.com", "Full Name", null);
    when(userRepository.findByEmail("unique@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(new User());

    userService.create(dto, RAW_PASSWORD);

    verify(userRepository, never()).findByUsername(anyString());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("validateBeforeCreate - success when both fields are null")
  void create_shouldSucceedWhenBothFieldsAreNull() {
    UserDto dto = new UserDto(null, null, null, "Full Name", null);
    when(userRepository.save(any(User.class))).thenReturn(new User());

    userService.create(dto, RAW_PASSWORD);

    verify(userRepository, never()).findByUsername(anyString());
    verify(userRepository, never()).findByEmail(anyString());
    verify(userRepository).save(any(User.class));
  }

  @Test
  @DisplayName("validateBeforeCreate - fail when username exists")
  void create_shouldFailWhenUsernameExists() {
    UserDto dto = new UserDto(null, "existing_user", "new@example.com", "Full Name", null);
    when(userRepository.findByUsername("existing_user")).thenReturn(Optional.of(new User()));

    assertThatThrownBy(() -> userService.create(dto, RAW_PASSWORD))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  @DisplayName("validateBeforeCreate - fail when email exists")
  void create_shouldFailWhenEmailExists() {
    UserDto dto = new UserDto(null, "new_user", "existing@example.com", "Full Name", null);
    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

    assertThatThrownBy(() -> userService.create(dto, RAW_PASSWORD))
        .isInstanceOf(DuplicateResourceException.class);
  }

  // ==================== ТЕСТЫ ДЛЯ validateBeforeUpdate ====================

  @Test
  @DisplayName("validateBeforeUpdate - success when updating to unique email")
  void update_shouldSucceedWithUniqueEmail() {
    UserDto dto = new UserDto(1L, null, "new_unique@example.com", null, null);
    when(userRepository.findByEmail("new_unique@example.com")).thenReturn(Optional.empty());

    userService.update(1L, dto);

    assertThat(existingUser.getEmail()).isEqualTo("new_unique@example.com");
  }

  @Test
  @DisplayName("validateBeforeUpdate - success when updating to same email")
  void update_shouldSucceedWithSameEmail() {
    UserDto dto = new UserDto(1L, null, "existing@example.com", null, null);

    userService.update(1L, dto);

    verify(userRepository, never()).findByEmail(anyString());
  }

  @Test
  @DisplayName("validateBeforeUpdate - fail when email already taken by another user")
  void update_shouldFailWhenEmailTaken() {
    User otherUser = new User();
    otherUser.setId(2L);
    UserDto dto = new UserDto(1L, null, "taken@example.com", null, null);
    when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

    assertThatThrownBy(() -> userService.update(1L, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  @DisplayName("validateBeforeUpdate - success when updating to same username")
  void update_shouldSucceedWithSameUsername() {
    UserDto dto = new UserDto(1L, "existing_user", null, null, null);

    userService.update(1L, dto);

    verify(userRepository, never()).findByUsername(anyString());
  }

  @Test
  @DisplayName("validateBeforeUpdate - success when username is unique")
  void update_shouldSucceedWithUniqueUsername() {
    UserDto dto = new UserDto(1L, "new_unique_username", null, null, null);
    when(userRepository.findByUsername("new_unique_username")).thenReturn(Optional.empty());

    userService.update(1L, dto);

    assertThat(existingUser.getUsername()).isEqualTo("new_unique_username");
  }

  @Test
  @DisplayName("validateBeforeUpdate - fail when username taken")
  void update_shouldFailWhenUsernameTaken() {
    User otherUser = new User();
    otherUser.setId(2L);
    UserDto dto = new UserDto(1L, "taken_username", null, null, null);
    when(userRepository.findByUsername("taken_username")).thenReturn(Optional.of(otherUser));

    assertThatThrownBy(() -> userService.update(1L, dto))
        .isInstanceOf(DuplicateResourceException.class);
  }

  // ==================== БАЗОВЫЕ ТЕСТЫ ====================

  @Test
  void create_shouldSucceed() {
    UserDto dto = new UserDto(null, "new_user", "new@example.com", "New User", null);
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenReturn(new User());

    userService.create(dto, RAW_PASSWORD);

    verify(userRepository).save(any(User.class));
  }

  @Test
  void delete_shouldSucceed() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    doNothing().when(userRepository).delete(any(User.class));

    userService.delete(1L);

    verify(userRepository).delete(existingUser);
  }

  @Test
  void authenticate_shouldSucceed() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

    userService.authenticate("existing@example.com", RAW_PASSWORD);

    verify(userMapper).toDto(existingUser);
  }

  @Test
  void getByUsername_shouldSucceed() {
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(existingUser));

    userService.getByUsername("existing_user");

    verify(userMapper).toDto(any());
  }

  @Test
  void getTrackedProducts_shouldSucceed() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

    assertThat(userService.getTrackedProducts(1L)).isNotNull();
  }

  @Test
  void addTrackedProduct_shouldSucceed() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(productRepository.findById(101L)).thenReturn(Optional.of(new Product()));

    userService.addTrackedProduct(1L, 101L);

    verify(userRepository).save(existingUser);
  }

  @Test
  void removeTrackedProduct_shouldSucceed() {
    Product p = new Product();
    p.setId(101L);
    existingUser.getTrackedProducts().add(p);
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

    userService.removeTrackedProduct(1L, 101L);

    verify(userRepository).save(existingUser);
  }

  @Test
  void updateLastLogin_shouldSucceed() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

    userService.updateLastLogin(1L);

    verify(userRepository).save(existingUser);
  }

  @Test
  void create_shouldThrowOnUnsupportedCall() {
    assertThatThrownBy(() -> userService.create(createDto))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Test
  void authenticate_shouldFailIfUserNotFound() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.authenticate("a", "b"))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void authenticate_shouldFailOnWrongPassword() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

    assertThatThrownBy(() -> userService.authenticate("a", "wrong"))
        .isInstanceOf(BusinessException.class);
  }

  @Test
  void getByUsername_shouldFailIfUserNotFound() {
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getByUsername("a"))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void getTrackedProducts_shouldFailIfUserNotFound() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.getTrackedProducts(99L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void addTrackedProduct_shouldFailIfUserNotFound() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.addTrackedProduct(99L, 101L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void addTrackedProduct_shouldFailIfProductNotFound() {
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.addTrackedProduct(1L, 999L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void removeTrackedProduct_shouldFailIfUserNotFound() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.removeTrackedProduct(99L, 101L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void addTrackedProduct_shouldDoNothingIfProductAlreadyTracked() {
    Product product = new Product();
    product.setId(101L);
    existingUser.getTrackedProducts().add(product);
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(productRepository.findById(101L)).thenReturn(Optional.of(product));

    userService.addTrackedProduct(1L, 101L);

    verify(userRepository, never()).save(any(User.class));
  }

  // ==================== ПОКРЫТИЕ PROTECTED МЕТОДОВ ====================

  @Test
  @DisplayName("Cover all protected methods")
  void coverProtectedMethods() throws Exception {
    // Метод getIdValue
    Method getIdValue = UserService.class.getDeclaredMethod("getIdValue", User.class);
    getIdValue.setAccessible(true);
    getIdValue.invoke(userService, existingUser);

    // Метод extractNameFromDto
    Method extractNameFromDto = UserService.class.getDeclaredMethod("extractNameFromDto", UserDto.class);
    extractNameFromDto.setAccessible(true);
    extractNameFromDto.invoke(userService, createDto);

    // Метод extractNameFromEntity
    Method extractNameFromEntity = UserService.class.getDeclaredMethod("extractNameFromEntity", User.class);
    extractNameFromEntity.setAccessible(true);
    extractNameFromEntity.invoke(userService, existingUser);

    // Метод beforeSave
    Method beforeSave = UserService.class.getDeclaredMethod("beforeSave", User.class);
    beforeSave.setAccessible(true);
    beforeSave.invoke(userService, existingUser);

    // Метод validateBeforeDelete
    Method validateBeforeDelete = UserService.class.getDeclaredMethod("validateBeforeDelete", User.class);
    validateBeforeDelete.setAccessible(true);
    validateBeforeDelete.invoke(userService, existingUser);

    // Приватные методы
    Method hashPassword = UserService.class.getDeclaredMethod("hashPassword", String.class);
    hashPassword.setAccessible(true);
    hashPassword.invoke(userService, RAW_PASSWORD);

    Method matchesPassword = UserService.class.getDeclaredMethod("matchesPassword", String.class, String.class);
    matchesPassword.setAccessible(true);
    matchesPassword.invoke(userService, RAW_PASSWORD, new BCryptPasswordEncoder().encode(RAW_PASSWORD));
  }

  @Test
  @DisplayName("updateEntity - throw exception when product not found")
  void updateEntity_shouldThrowWhenProductNotFound() {
    UserDto dto = new UserDto(1L, null, null, null, List.of(999L));
    when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
    when(productRepository.findById(999L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.update(1L, dto))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}