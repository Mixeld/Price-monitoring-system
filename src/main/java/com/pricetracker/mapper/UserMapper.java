package com.pricetracker.mapper;

import com.pricetracker.dto.UserDto;
import com.pricetracker.entity.Product;
import com.pricetracker.entity.User;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserDto toDto(User user) {
    if (user == null) return null;

    List<Long> productIds = (user.getTrackedProducts() != null)
        ? user.getTrackedProducts().stream().map(Product::getId).collect(Collectors.toList())
        : Collections.emptyList();

    return new UserDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        productIds
    );
  }

  public User toEntity(UserDto dto) {
    if (dto == null) return null;

    User user = new User();
    user.setId(dto.id());
    user.setUsername(dto.username());
    user.setEmail(dto.email());

    return user;
  }
}
