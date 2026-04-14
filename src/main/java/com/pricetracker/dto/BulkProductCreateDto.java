package com.pricetracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "Bulk Product Creation DTO")
public record BulkProductCreateDto(
    @Schema(description = "List of products to create", required = true)
    @NotEmpty(message = "Product list cannot be empty")
    @Valid
    List<ProductDto> products
) {}