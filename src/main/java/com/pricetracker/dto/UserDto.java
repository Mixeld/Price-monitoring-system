package com.pricetracker.dto;

import java.util.List;

public record UserDto(
    Long id,
    String username,
    String email,
    List<Long> subscribedProductIds
) {

}
