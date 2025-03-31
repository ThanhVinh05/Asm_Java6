package com.poly.controller.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCreationRequest {
    @NotBlank(message = "Category name is required")
    private String categoryName;
}
