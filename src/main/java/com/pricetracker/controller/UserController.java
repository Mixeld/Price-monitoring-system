package com.pricetracker.controller;

import com.pricetracker.dto.UserCreateDto;
import com.pricetracker.dto.UserDto;
import com.pricetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "User management endpoints")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @Operation(summary = "Register a new user", description = "Creates a new user account")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Username or email already exists")
  })
  @PostMapping("/register")
  public ResponseEntity<UserDto> register(
      @Parameter(description = "User registration data", required = true)
      @Valid @RequestBody UserCreateDto createDto) {
    UserDto userDto = new UserDto(
        null,
        createDto.username(),
        createDto.email(),
        createDto.fullName(),
        null
    );

    UserDto created = userService.create(userDto, createDto.password());
    return ResponseEntity
        .created(URI.create("/api/users/" + created.id()))
        .body(created);
  }

  @Operation(summary = "User login", description = "Authenticates a user with email and password")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Login successful",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @PostMapping("/login")
  public ResponseEntity<UserDto> login(
      @Parameter(description = "User email", example = "john@example.com", required = true)
      @RequestParam String email,
      @Parameter(description = "User password", example = "securePass123", required = true)
      @RequestParam String password) {
    return ResponseEntity.ok(userService.authenticate(email, password));
  }

  @Operation(summary = "Get user by ID", description = "Returns a single user by their ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User found",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<UserDto> getUserById(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id) {
    return ResponseEntity.ok(userService.getById(id));
  }

  @Operation(summary = "Get user by username", description = "Returns a single user by their username")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User found",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @GetMapping("/username/{username}")
  public ResponseEntity<UserDto> getUserByUsername(
      @Parameter(description = "Username", example = "john_doe", required = true)
      @PathVariable String username) {
    return ResponseEntity.ok(userService.getByUsername(username));
  }

  @Operation(summary = "Get all users", description = "Returns a list of all users")
  @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
  @GetMapping
  public ResponseEntity<List<UserDto>> getAllUsers() {
    return ResponseEntity.ok(userService.getAll());
  }

  @Operation(summary = "Update a user", description = "Updates an existing user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User updated successfully",
          content = @Content(schema = @Schema(implementation = UserDto.class))),
      @ApiResponse(responseCode = "404", description = "User not found"),
      @ApiResponse(responseCode = "400", description = "Invalid input data"),
      @ApiResponse(responseCode = "409", description = "Username or email already exists")
  })
  @PutMapping("/{id}")
  public ResponseEntity<UserDto> updateUser(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id,
      @Parameter(description = "Updated user data", required = true)
      @Valid @RequestBody UserDto userDto) {
    return ResponseEntity.ok(userService.update(id, userDto));
  }

  @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "User deleted successfully"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Get tracked products", description = "Returns a list of products tracked by a user")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Tracked products retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @GetMapping("/{id}/tracked-products")
  public ResponseEntity<?> getTrackedProducts(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long id) {
    return ResponseEntity.ok(userService.getTrackedProducts(id));
  }

  @Operation(summary = "Add tracked product", description = "Adds a product to user's tracked list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Product added successfully"),
      @ApiResponse(responseCode = "404", description = "User or product not found")
  })
  @PostMapping("/{userId}/track/{productId}")
  public ResponseEntity<Void> addTrackedProduct(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long userId,
      @Parameter(description = "Product ID", example = "1", required = true)
      @PathVariable Long productId) {
    userService.addTrackedProduct(userId, productId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Remove tracked product", description = "Removes a product from user's tracked list")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Product removed successfully"),
      @ApiResponse(responseCode = "404", description = "User not found")
  })
  @DeleteMapping("/{userId}/untrack/{productId}")
  public ResponseEntity<Void> removeTrackedProduct(
      @Parameter(description = "User ID", example = "1", required = true)
      @PathVariable Long userId,
      @Parameter(description = "Product ID", example = "1", required =true)
      @PathVariable Long productId) {
    userService.removeTrackedProduct(userId, productId);
    return ResponseEntity.noContent().build();
  }
}