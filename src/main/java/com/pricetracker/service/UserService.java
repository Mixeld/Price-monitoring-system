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
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class UserService extends NamedEntityService<User, UserDto, Long> {

  private final UserRepository userRepository;
  private final UserMapper mapper;
  private final ProductRepository productRepository;
  private final UserService self;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserService(UserRepository userRepository,
      UserMapper mapper,
      ProductRepository productRepository,
      @Lazy UserService self) {
    super(userRepository, "User", mapper::toDto, mapper::toEntity,
        userRepository::findByEmail, self);
    this.userRepository = userRepository;
    this.mapper = mapper;
    this.productRepository = productRepository;
    this.self = self;
    this.passwordEncoder = new BCryptPasswordEncoder();
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
    if (dto.username() != null && userRepository.findByUsername(dto.username()).isPresent()) {
      throw new DuplicateResourceException("User", "username", dto.username());
    }

    if (dto.email() != null && userRepository.findByEmail(dto.email()).isPresent()) {
      throw new DuplicateResourceException("User", "email", dto.email());
    }
  }

  @Override
  protected void validateBeforeUpdate(Long id, UserDto dto, User entity) {
    // Проверка email
    if (dto.email() != null &&
        !dto.email().equals(entity.getEmail()) &&
        userRepository.findByEmail(dto.email()).isPresent()) {
      throw new DuplicateResourceException("User", "email", dto.email());
    }

    // Проверка username
    if (dto.username() != null &&
        !dto.username().equals(entity.getUsername()) &&
        userRepository.findByUsername(dto.username()).isPresent()) {
      throw new DuplicateResourceException("User", "username", dto.username());
    }
  }

  @Override
  protected void validateBeforeDelete(User entity) {
    // можно добавить логику
  }

  @Override
  protected void updateEntity(User entity, UserDto dto) {
    if (dto.username() != null) {
      entity.setUsername(dto.username());
    }
    if (dto.email() != null) {
      entity.setEmail(dto.email());
    }
    if (dto.fullName() != null) {
      entity.setFullName(dto.fullName());
    }
    entity.setUpdatedAt(LocalDateTime.now());

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
  }

  private String hashPassword(String password) {
    return passwordEncoder.encode(password);
  }

  private boolean matchesPassword(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  @Transactional
  public UserDto create(UserDto dto, String rawPassword) {
    log.debug("Creating new user with data: {}", dto);
    validateBeforeCreate(dto);

    User entity = mapper.toEntity(dto);
    entity.setPasswordHash(hashPassword(rawPassword));
    beforeSave(entity);
    User savedEntity = userRepository.save(entity);
    afterSave(savedEntity);

    log.info("User created successfully with id: {}", savedEntity.getId());
    return mapper.toDto(savedEntity);
  }

  @Override
  @Transactional
  public UserDto create(UserDto dto) {
    throw new UnsupportedOperationException(
        "Use create(UserDto, String password) instead. Password is required.");
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
        .orElseThrow(() -> new BusinessException("Invalid credentials", "AUTH_001"));

    if (!matchesPassword(password, user.getPasswordHash())) {
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