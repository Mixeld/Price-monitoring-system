package com.pricetracker.controller;

import com.pricetracker.dto.UserCreateDto;
import com.pricetracker.dto.UserDto;
import com.pricetracker.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<UserDto> register(@Valid @RequestBody UserCreateDto createDto) {
    UserDto userDto = new UserDto(
        null,
        createDto.username(),
        createDto.email(),
        createDto.fullName(),
        null
    );

    // Передаём DTO и пароль отдельно
    UserDto created = userService.create(userDto, createDto.password());
    return ResponseEntity
        .created(URI.create("/api/users/" + created.id()))
        .body(created);
  }

  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @RequestParam String email,
      @RequestParam String password) {
    return ResponseEntity.ok(userService.authenticate(email, password));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @GetMapping("/username/{username}")
  public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
    return ResponseEntity.ok(userService.getByUsername(username));
  }

  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAll());
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @PathVariable Long id,
      @Valid @RequestBody UserDto userDto) {
    return ResponseEntity.ok(userService.update(id, userDto));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}/tracked-products")
  public ResponseEntity<?> getTrackedProducts(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getTrackedProducts(id));
  }

  @PostMapping("/{userId}/track/{productId}")
  public ResponseEntity<Void> addTrackedProduct(
      @PathVariable Long userId,
      @PathVariable Long productId) {
    userService.addTrackedProduct(userId, productId);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{userId}/untrack/{productId}")
  public ResponseEntity<Void> removeTrackedProduct(
      @PathVariable Long userId,
      @PathVariable Long productId) {
    userService.removeTrackedProduct(userId, productId);
    return ResponseEntity.noContent().build();
  }
}