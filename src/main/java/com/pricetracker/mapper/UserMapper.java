package com.pricetracker.mapper;

import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

  public UserDto toDto(User user) {
    if (user == null) {
      return null;
    }

    List<Long> trackedProductIds = user.getTrackedProducts() != null
        ? user.getTrackedProducts().stream()
        .map(Product::getId)
        .collect(Collectors.toList())
        : List.of();

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getFullName(),
        null, // Не передаем пароль в DTO
        trackedProductIds
    );
  }

  public User toEntity(UserDto dto) {
    if (dto == null) {
      return null;
    }

    User user = new User();
    user.setId(dto.id());
    user.setUsername(dto.username());
    user.setEmail(dto.email());
    user.setFullName(dto.fullName());
    user.setPasswordHash(dto.password()); // Будет закодирован позже в сервисе

    // trackedProducts будет установлен отдельно в сервисе

    return user;
  }
}