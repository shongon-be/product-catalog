package com.shongon.catalog.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProductRequest {
    @NotBlank(message = "Product name cannot be blank")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Product description cannot be blank")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Product price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "999999.99", message = "Price cannot exceed $999,999.99")
    private Double price;

    @NotBlank(message = "Product category cannot be blank")
    @Pattern(regexp = "^(ELECTRONICS|CLOTHING|FOOD|BOOKS|HOME|PREMIUM)$",
            message = "Category must be one of: ELECTRONICS, CLOTHING, FOOD, BOOKS, HOME, PREMIUM")
    private String category;
}
