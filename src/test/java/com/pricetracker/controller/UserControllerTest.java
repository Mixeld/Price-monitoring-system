package com.pricetracker.controller;

import com.pricetracker.dto.UserCreateDto;
import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit-тесты для UserController")
class UserControllerTest {

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  private UserDto userDto;
  private UserCreateDto userCreateDto;
  private List<UserDto> userDtoList;
  private List<Product> trackedProducts;

  @BeforeEach
  void setUp() {
    userDto = new UserDto(1L, "john_doe", "john@example.com", "John Doe", List.of(101L, 102L));
    userCreateDto = new UserCreateDto("john_doe", "john@example.com", "John Doe", "password123");
    userDtoList = List.of(userDto);

    Product product1 = new Product();
    product1.setId(101L);
    product1.setName("iPhone 15");

    Product product2 = new Product();
    product2.setId(102L);
    product2.setName("MacBook Pro");

    trackedProducts = List.of(product1, product2);
  }

  // ==================== ТЕСТЫ ДЛЯ register ====================

  @Nested
  @DisplayName("Тесты метода register(UserCreateDto createDto)")
  class RegisterTests {

    @Test
    @DisplayName("[УСПЕХ] Регистрация нового пользователя")
    void register_shouldCreateAndReturnCreatedUser() {
      UserDto createdUser = new UserDto(1L, "john_doe", "john@example.com", "John Doe", List.of());
      when(userService.create(any(UserDto.class), eq("password123"))).thenReturn(createdUser);

      ResponseEntity<UserDto> response = userController.register(userCreateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody()).isEqualTo(createdUser);
      assertThat(response.getHeaders().getLocation()).isEqualTo(URI.create("/api/users/1"));

      ArgumentCaptor<UserDto> userDtoCaptor = ArgumentCaptor.forClass(UserDto.class);
      verify(userService).create(userDtoCaptor.capture(), eq("password123"));

      UserDto capturedDto = userDtoCaptor.getValue();
      assertThat(capturedDto.id()).isNull();
      assertThat(capturedDto.username()).isEqualTo("john_doe");
      assertThat(capturedDto.email()).isEqualTo("john@example.com");
      assertThat(capturedDto.fullName()).isEqualTo("John Doe");
      assertThat(capturedDto.trackedProductIds()).isNull();
    }

    @Test
    @DisplayName("[УСПЕХ] Регистрация с минимальными полями")
    void register_shouldRegisterWithMinimalFields() {
      UserCreateDto minimalCreateDto = new UserCreateDto("user", "user@test.com", "User", "pass123");
      UserDto createdUser = new UserDto(2L, "user", "user@test.com", "User", List.of());
      when(userService.create(any(UserDto.class), eq("pass123"))).thenReturn(createdUser);

      ResponseEntity<UserDto> response = userController.register(minimalCreateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      assertThat(response.getBody().username()).isEqualTo("user");
      verify(userService).create(any(UserDto.class), eq("pass123"));
    }
  }

  // ==================== ТЕСТЫ ДЛЯ login ====================

  @Nested
  @DisplayName("Тесты метода login(String email, String password)")
  class LoginTests {

    @Test
    @DisplayName("[УСПЕХ] Вход пользователя")
    void login_shouldAuthenticateAndReturnUser() {
      when(userService.authenticate("john@example.com", "password123")).thenReturn(userDto);

      ResponseEntity<UserDto> response = userController.login("john@example.com", "password123");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(userDto);
      verify(userService).authenticate("john@example.com", "password123");
    }

    @Test
    @DisplayName("[УСПЕХ] Вход с email в верхнем регистре")
    void login_shouldHandleUppercaseEmail() {
      when(userService.authenticate("JOHN@EXAMPLE.COM", "password123")).thenReturn(userDto);

      ResponseEntity<UserDto> response = userController.login("JOHN@EXAMPLE.COM", "password123");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(userService).authenticate("JOHN@EXAMPLE.COM", "password123");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getUserById ====================

  @Nested
  @DisplayName("Тесты метода getUserById(Long id)")
  class GetUserByIdTests {

    @Test
    @DisplayName("[УСПЕХ] Получение пользователя по ID")
    void getUserById_shouldReturnUser_whenExists() {
      when(userService.getById(1L)).thenReturn(userDto);

      ResponseEntity<UserDto> response = userController.getUserById(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(userDto);
      verify(userService).getById(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пользователя с ID = 0")
    void getUserById_shouldHandleZeroId() {
      UserDto zeroIdUser = new UserDto(0L, "zero", "zero@test.com", "Zero User", List.of());
      when(userService.getById(0L)).thenReturn(zeroIdUser);

      ResponseEntity<UserDto> response = userController.getUserById(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(0L);
      verify(userService).getById(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пользователя с большим ID")
    void getUserById_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      UserDto largeIdUser = new UserDto(largeId, "large", "large@test.com", "Large User", List.of());
      when(userService.getById(largeId)).thenReturn(largeIdUser);

      ResponseEntity<UserDto> response = userController.getUserById(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().id()).isEqualTo(largeId);
      verify(userService).getById(largeId);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getUserByUsername ====================

  @Nested
  @DisplayName("Тесты метода getUserByUsername(String username)")
  class GetUserByUsernameTests {

    @Test
    @DisplayName("[УСПЕХ] Получение пользователя по имени")
    void getUserByUsername_shouldReturnUser_whenExists() {
      when(userService.getByUsername("john_doe")).thenReturn(userDto);

      ResponseEntity<UserDto> response = userController.getUserByUsername("john_doe");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(userDto);
      verify(userService).getByUsername("john_doe");
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пользователя с username в верхнем регистре")
    void getUserByUsername_shouldHandleUppercaseUsername() {
      when(userService.getByUsername("JOHN_DOE")).thenReturn(userDto);

      ResponseEntity<UserDto> response = userController.getUserByUsername("JOHN_DOE");

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(userService).getByUsername("JOHN_DOE");
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getAllUsers ====================

  @Nested
  @DisplayName("Тесты метода getAllUsers()")
  class GetAllUsersTests {

    @Test
    @DisplayName("[УСПЕХ] Получение всех пользователей")
    void getAllUsers_shouldReturnAllUsers() {
      when(userService.getAll()).thenReturn(userDtoList);

      ResponseEntity<List<UserDto>> response = userController.getAllUsers();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(1);
      assertThat(response.getBody().get(0)).isEqualTo(userDto);
      verify(userService).getAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка пользователей")
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
      when(userService.getAll()).thenReturn(List.of());

      ResponseEntity<List<UserDto>> response = userController.getAllUsers();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEmpty();
      verify(userService).getAll();
    }

    @Test
    @DisplayName("[УСПЕХ] Получение нескольких пользователей")
    void getAllUsers_shouldReturnMultipleUsers() {
      UserDto user2 = new UserDto(2L, "jane_doe", "jane@example.com", "Jane Doe", List.of());
      List<UserDto> multipleUsers = List.of(userDto, user2);
      when(userService.getAll()).thenReturn(multipleUsers);

      ResponseEntity<List<UserDto>> response = userController.getAllUsers();

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).hasSize(2);
      verify(userService).getAll();
    }
  }

  // ==================== ТЕСТЫ ДЛЯ updateUser ====================

  @Nested
  @DisplayName("Тесты метода updateUser(Long id, UserDto userDto)")
  class UpdateUserTests {

    @Test
    @DisplayName("[УСПЕХ] Обновление пользователя")
    void updateUser_shouldUpdateAndReturnUpdatedUser() {
      UserDto updatedUser = new UserDto(1L, "john_updated", "john_updated@example.com", "John Updated", List.of());
      when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

      ResponseEntity<UserDto> response = userController.updateUser(1L, userDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(updatedUser);
      verify(userService).update(1L, userDto);
    }

    @Test
    @DisplayName("[УСПЕХ] Обновление пользователя с новым именем")
    void updateUser_shouldUpdateUsername() {
      UserDto updateDto = new UserDto(1L, "new_username", null, null, null);
      UserDto updatedUser = new UserDto(1L, "new_username", "john@example.com", "John Doe", List.of());
      when(userService.update(eq(1L), any(UserDto.class))).thenReturn(updatedUser);

      ResponseEntity<UserDto> response = userController.updateUser(1L, updateDto);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody().username()).isEqualTo("new_username");
      verify(userService).update(1L, updateDto);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ deleteUser ====================

  @Nested
  @DisplayName("Тесты метода deleteUser(Long id)")
  class DeleteUserTests {

    @Test
    @DisplayName("[УСПЕХ] Удаление пользователя")
    void deleteUser_shouldDeleteAndReturnNoContent() {
      doNothing().when(userService).delete(1L);

      ResponseEntity<Void> response = userController.deleteUser(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).delete(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление пользователя с ID = 0")
    void deleteUser_shouldHandleZeroId() {
      doNothing().when(userService).delete(0L);

      ResponseEntity<Void> response = userController.deleteUser(0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).delete(0L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление пользователя с большим ID")
    void deleteUser_shouldHandleLargeId() {
      Long largeId = Long.MAX_VALUE;
      doNothing().when(userService).delete(largeId);

      ResponseEntity<Void> response = userController.deleteUser(largeId);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).delete(largeId);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ getTrackedProducts ====================

  @Nested
  @DisplayName("Тесты метода getTrackedProducts(Long id)")
  class GetTrackedProductsTests {

    @Test
    @DisplayName("[УСПЕХ] Получение отслеживаемых продуктов")
    void getTrackedProducts_shouldReturnTrackedProducts() {
      when(userService.getTrackedProducts(1L)).thenReturn(trackedProducts);

      ResponseEntity<?> response = userController.getTrackedProducts(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(trackedProducts);
      verify(userService).getTrackedProducts(1L);
    }

    @Test
    @DisplayName("[УСПЕХ] Получение пустого списка отслеживаемых продуктов")
    void getTrackedProducts_shouldReturnEmptyList_whenNoTrackedProducts() {
      when(userService.getTrackedProducts(1L)).thenReturn(List.of());

      ResponseEntity<?> response = userController.getTrackedProducts(1L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isEqualTo(List.of());
      verify(userService).getTrackedProducts(1L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ addTrackedProduct ====================

  @Nested
  @DisplayName("Тесты метода addTrackedProduct(Long userId, Long productId)")
  class AddTrackedProductTests {

    @Test
    @DisplayName("[УСПЕХ] Добавление продукта в отслеживаемые")
    void addTrackedProduct_shouldAddProductAndReturnOk() {
      doNothing().when(userService).addTrackedProduct(1L, 101L);

      ResponseEntity<Void> response = userController.addTrackedProduct(1L, 101L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(userService).addTrackedProduct(1L, 101L);
    }

    @Test
    @DisplayName("[УСПЕХ] Добавление продукта с userId = 0")
    void addTrackedProduct_shouldHandleZeroUserId() {
      doNothing().when(userService).addTrackedProduct(0L, 101L);

      ResponseEntity<Void> response = userController.addTrackedProduct(0L, 101L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(userService).addTrackedProduct(0L, 101L);
    }

    @Test
    @DisplayName("[УСПЕХ] Добавление продукта с productId = 0")
    void addTrackedProduct_shouldHandleZeroProductId() {
      doNothing().when(userService).addTrackedProduct(1L, 0L);

      ResponseEntity<Void> response = userController.addTrackedProduct(1L, 0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      verify(userService).addTrackedProduct(1L, 0L);
    }
  }

  // ==================== ТЕСТЫ ДЛЯ removeTrackedProduct ====================

  @Nested
  @DisplayName("Тесты метода removeTrackedProduct(Long userId, Long productId)")
  class RemoveTrackedProductTests {

    @Test
    @DisplayName("[УСПЕХ] Удаление продукта из отслеживаемых")
    void removeTrackedProduct_shouldRemoveProductAndReturnNoContent() {
      doNothing().when(userService).removeTrackedProduct(1L, 101L);

      ResponseEntity<Void> response = userController.removeTrackedProduct(1L, 101L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).removeTrackedProduct(1L, 101L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление продукта с userId = 0")
    void removeTrackedProduct_shouldHandleZeroUserId() {
      doNothing().when(userService).removeTrackedProduct(0L, 101L);

      ResponseEntity<Void> response = userController.removeTrackedProduct(0L, 101L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).removeTrackedProduct(0L, 101L);
    }

    @Test
    @DisplayName("[УСПЕХ] Удаление продукта с productId = 0")
    void removeTrackedProduct_shouldHandleZeroProductId() {
      doNothing().when(userService).removeTrackedProduct(1L, 0L);

      ResponseEntity<Void> response = userController.removeTrackedProduct(1L, 0L);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
      verify(userService).removeTrackedProduct(1L, 0L);
    }
  }

  // ==================== ИНТЕГРАЦИОННЫЕ ТЕСТЫ ====================

  @Nested
  @DisplayName("Интеграционные тесты (цепочки вызовов)")
  class IntegrationTests {

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Полный CRUD цикл для пользователя")
    void fullCrudCycle_shouldWorkCorrectly() {
      // 1. Register
      UserCreateDto newUser = new UserCreateDto("test_user", "test@example.com", "Test User", "password");
      UserDto createdUser = new UserDto(10L, "test_user", "test@example.com", "Test User", List.of());
      when(userService.create(any(UserDto.class), eq("password"))).thenReturn(createdUser);

      ResponseEntity<UserDto> registerResponse = userController.register(newUser);
      assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
      Long createdId = registerResponse.getBody().id();

      // 2. Get by ID
      when(userService.getById(createdId)).thenReturn(createdUser);
      ResponseEntity<UserDto> getResponse = userController.getUserById(createdId);
      assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(getResponse.getBody().username()).isEqualTo("test_user");

      // 3. Update
      UserDto updateDto = new UserDto(createdId, "updated_user", "updated@example.com", "Updated User", null);
      UserDto updatedUser = new UserDto(createdId, "updated_user", "updated@example.com", "Updated User", List.of());
      when(userService.update(eq(createdId), any(UserDto.class))).thenReturn(updatedUser);

      ResponseEntity<UserDto> updateResponse = userController.updateUser(createdId, updateDto);
      assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(updateResponse.getBody().username()).isEqualTo("updated_user");

      // 4. Add tracked product
      doNothing().when(userService).addTrackedProduct(createdId, 101L);
      ResponseEntity<Void> addResponse = userController.addTrackedProduct(createdId, 101L);
      assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

      // 5. Get tracked products
      when(userService.getTrackedProducts(createdId)).thenReturn(trackedProducts);
      ResponseEntity<?> getTrackedResponse = userController.getTrackedProducts(createdId);
      assertThat(getTrackedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

      // 6. Remove tracked product
      doNothing().when(userService).removeTrackedProduct(createdId, 101L);
      ResponseEntity<Void> removeResponse = userController.removeTrackedProduct(createdId, 101L);
      assertThat(removeResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      // 7. Delete user
      doNothing().when(userService).delete(createdId);
      ResponseEntity<Void> deleteResponse = userController.deleteUser(createdId);
      assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

      verify(userService).create(any(UserDto.class), eq("password"));
      verify(userService).getById(createdId);
      verify(userService).update(eq(createdId), any(UserDto.class));
      verify(userService).addTrackedProduct(createdId, 101L);
      verify(userService).getTrackedProducts(createdId);
      verify(userService).removeTrackedProduct(createdId, 101L);
      verify(userService).delete(createdId);
    }

    @Test
    @DisplayName("[ИНТЕГРАЦИЯ] Регистрация и вход пользователя")
    void registerAndLogin_shouldWorkCorrectly() {
      // 1. Register
      UserCreateDto newUser = new UserCreateDto("auth_user", "auth@example.com", "Auth User", "secret");
      UserDto createdUser = new UserDto(5L, "auth_user", "auth@example.com", "Auth User", List.of());
      when(userService.create(any(UserDto.class), eq("secret"))).thenReturn(createdUser);

      ResponseEntity<UserDto> registerResponse = userController.register(newUser);
      assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

      // 2. Login
      when(userService.authenticate("auth@example.com", "secret")).thenReturn(createdUser);
      ResponseEntity<UserDto> loginResponse = userController.login("auth@example.com", "secret");
      assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(loginResponse.getBody().username()).isEqualTo("auth_user");

      verify(userService).create(any(UserDto.class), eq("secret"));
      verify(userService).authenticate("auth@example.com", "secret");
    }
  }
}