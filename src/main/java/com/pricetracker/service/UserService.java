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
import com.pricetracker.service.base.NamedEntityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService extends NamedEntityService<User, UserDto, Long> {

  private final UserRepository userRepository;
  private final UserMapper mapper;
  private final ProductRepository productRepository;

  public UserService(UserRepository userRepository,
      UserMapper mapper,
      ProductRepository productRepository) {
    super(userRepository, "User", mapper::toDto, mapper::toEntity, userRepository::findByEmail);
    this.userRepository = userRepository;
    this.mapper = mapper;
    this.productRepository = productRepository;
  }

  @Override
  protected Long getIdValue(User entity) {
    return entity.getId();
  }

  @Override
  protected String extractNameFromDto(UserDto dto) {
    return dto.email();
  }

  @Override
  protected String extractNameFromEntity(User entity) {
    return entity.getEmail();
  }

  @Override
  protected void validateBeforeCreate(UserDto dto) {
    super.validateBeforeCreate(dto);

    if (dto.username() != null && userRepository.findByUsername(dto.username()).isPresent()) {
      throw new DuplicateResourceException("User", "username", dto.username());
    }
  }

  @Override
  protected void validateBeforeUpdate(Long id, UserDto dto, User entity) {
    super.validateBeforeUpdate(id, dto, entity);

    if (dto.username() != null &&
        !dto.username().equals(entity.getUsername()) &&
        userRepository.findByUsername(dto.username()).isPresent()) {
      throw new DuplicateResourceException("User", "username", dto.username());
    }
  }

  @Override
  protected void validateBeforeDelete(User entity) {
    // Можно добавить проверки перед удалением пользователя
  }

  @Override
  protected void updateEntity(User entity, UserDto dto) {
    entity.setUsername(dto.username());
    entity.setEmail(dto.email());
    entity.setFullName(dto.fullName());
    entity.setUpdatedAt(LocalDateTime.now());

    if (dto.password() != null && !dto.password().isBlank()) {
      entity.setPasswordHash(hashPassword(dto.password()));
    }

    // Обновляем отслеживаемые продукты
    if (dto.trackedProductIds() != null) {
      List<Product> products = dto.trackedProductIds().stream()
          .map(productId -> productRepository.findById(productId)
              .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId)))
          .toList();
      entity.setTrackedProducts(products);
    }
  }

  @Override
  protected void beforeSave(User entity) {
    entity.setCreatedAt(LocalDateTime.now());
    entity.setUpdatedAt(LocalDateTime.now());
    if (entity.getPasswordHash() != null) {
      entity.setPasswordHash(hashPassword(entity.getPasswordHash()));
    }
  }

  private String hashPassword(String password) {
    return org.springframework.util.DigestUtils.md5DigestAsHex(password.getBytes());
  }

  public UserDto getByUsername(String username) {
    log.debug("Getting user by username: {}", username);
    return userRepository.findByUsername(username)
        .map(mapper::toDto)
        .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
  }

  public UserDto authenticate(String email, String password) {
    log.debug("Authenticating user with email: {}", email);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

    String hashedPassword = hashPassword(password);
    if (!hashedPassword.equals(user.getPasswordHash())) {
      throw new BusinessException("Invalid credentials", "AUTH_001");
    }

    return mapper.toDto(user);
  }

  @Transactional
  public void updateLastLogin(Long id) {
    log.debug("Updating last login for user: {}", id);
    userRepository.findById(id).ifPresent(user -> {
      user.setLastLogin(LocalDateTime.now());
      userRepository.save(user);
    });
  }

  // Новые методы для работы с отслеживаемыми продуктами

  @Transactional(readOnly = true)
  public List<Product> getTrackedProducts(Long userId) {
    log.debug("Getting tracked products for user: {}", userId);
    User user = findEntityById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    return user.getTrackedProducts();
  }

  @Transactional
  public void addTrackedProduct(Long userId, Long productId) {
    log.debug("Adding product {} to tracked products for user: {}", productId, userId);

    User user = findEntityById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

    if (!user.getTrackedProducts().contains(product)) {
      user.getTrackedProducts().add(product);
      userRepository.save(user);
    }
  }

  @Transactional
  public void removeTrackedProduct(Long userId, Long productId) {
    log.debug("Removing product {} from tracked products for user: {}", productId, userId);

    User user = findEntityById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

    user.getTrackedProducts().removeIf(product -> product.getId().equals(productId));
    userRepository.save(user);
  }
}
