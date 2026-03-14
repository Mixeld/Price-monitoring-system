package com.pricetracker.service;

import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.User;
import com.pricetracker.mapper.UserMapper;
import com.pricetracker.repository.ProductRepository;
import com.pricetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

  private static final String USER_NOT_FOUND = "User not found with id: ";
  
  private final UserRepository userRepository;
  private final ProductRepository productRepository;
  private final UserMapper userMapper;

  @Transactional(readOnly = true)
  public List<UserDto> getAllUsers() {
    log.debug("Getting all users");
    return userRepository.findAll().stream()
        .map(userMapper::toDto)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public UserDto getUserById(Long id) {
    log.debug("Getting user by id: {}", id);
    return userRepository.findById(id)
        .map(userMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));
  }

  @Transactional(readOnly = true)
  public UserDto getUserByUsername(String username) {
    log.debug("Getting user by username: {}", username);
    return userRepository.findByUsername(username)
        .map(userMapper::toDto)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + username));
  }

  @Transactional
  public UserDto createUser(UserDto dto) {
    log.debug("Creating new user with username: {}", dto.username());

    // Проверка на существующего пользователя
    if (userRepository.findByUsername(dto.username()).isPresent()) {
      throw new IllegalArgumentException("User with username " + dto.username() + " already exists");
    }

    // Проверка email (можно добавить)
    if (dto.email() != null && userRepository.findByEmail(dto.email()).isPresent()) {
      throw new IllegalArgumentException("User with email " + dto.email() + " already exists");
    }

    User user = userMapper.toEntity(dto);
    User savedUser = userRepository.save(user);
    log.info("User created successfully with id: {}", savedUser.getId());

    return userMapper.toDto(savedUser);
  }

  @Transactional
  public UserDto updateUser(Long id, UserDto dto) {
    log.debug("Updating user with id: {}", id);

    User user = userRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + id));

    // Проверка уникальности username (если он меняется)
    if (!user.getUsername().equals(dto.username()) &&
        userRepository.findByUsername(dto.username()).isPresent()) {
      throw new IllegalArgumentException("Username " + dto.username() + " is already taken");
    }

    // Проверка уникальности email (если он меняется)
    if (dto.email() != null && !dto.email().equals(user.getEmail()) &&
        userRepository.findByEmail(dto.email()).isPresent()) {
      throw new IllegalArgumentException("Email " + dto.email() + " is already registered");
    }

    user.setUsername(dto.username());
    user.setEmail(dto.email());

    // Сохранять не обязательно - @Transactional сделает это автоматически
    // userRepository.save(user);

    log.info("User updated successfully with id: {}", id);
    return userMapper.toDto(user);
  }

  @Transactional
  public void deleteUser(Long id) {
    log.debug("Deleting user with id: {}", id);

    if (!userRepository.existsById(id)) {
      throw new EntityNotFoundException(USER_NOT_FOUND + id);
    }

    userRepository.deleteById(id);
    log.info("User deleted successfully with id: {}", id);
  }

  @Transactional
  public void subscribeToProduct(Long userId, Long productId) {
    log.debug("Subscribing user {} to product {}", userId, productId);

    User user = userRepository.findWithProductsById(userId)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

    if (!user.getTrackedProducts().contains(product)) {
      user.getTrackedProducts().add(product);
      log.info("User {} subscribed to product {}", userId, productId);
      // Не нужно вызывать save - @Transactional сохранит изменения
    } else {
      log.debug("User {} already subscribed to product {}", userId, productId);
    }
  }

  @Transactional
  public void unsubscribeFromProduct(Long userId, Long productId) {
    log.debug("Unsubscribing user {} from product {}", userId, productId);

    User user = userRepository.findWithProductsById(userId)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    boolean removed = user.getTrackedProducts().removeIf(p -> p.getId().equals(productId));

    if (removed) {
      log.info("User {} unsubscribed from product {}", userId, productId);
      // Не нужно вызывать save - @Transactional сохранит изменения
    } else {
      log.debug("User {} was not subscribed to product {}", userId, productId);
    }
  }

  @Transactional(readOnly = true)
  public List<Long> getUserSubscriptions(Long userId) {
    log.debug("Getting subscriptions for user {}", userId);

    User user = userRepository.findWithProductsById(userId)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    return user.getTrackedProducts().stream()
        .map(Product::getId)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<Product> getUserTrackedProducts(Long userId) {
    log.debug("Getting tracked products for user {}", userId);

    User user = userRepository.findWithProductsById(userId)
        .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND + userId));

    return user.getTrackedProducts();
  }
}
