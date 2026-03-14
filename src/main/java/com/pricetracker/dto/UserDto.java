package com.pricetracker.dto;

import java.util.List;

public record UserDto(
    Long id,
    String username,
    String email,
    String fullName,
    String password,
    List<Long> trackedProductIds  // Добавляем поле для отслеживаемых продуктов
) {}