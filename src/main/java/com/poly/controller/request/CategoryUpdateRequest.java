package com.poly.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryUpdateRequest {
    @Min(value = 1, message = "Category ID must be equals or greater than 1")
    private Long id;
    @NotBlank(message = "Category name is required")
    private String categoryName;
}
