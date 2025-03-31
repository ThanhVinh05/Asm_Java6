package com.poly.controller;

import com.poly.controller.request.CategoryCreationRequest;
import com.poly.controller.request.CategoryUpdateRequest;
import com.poly.controller.response.CategoryResponse;
import com.poly.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/category")
@Tag(name = "Category Controller")
@Slf4j(topic = "CATEGORY-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Get category list", description = "API retrieve all categories from database")
    @GetMapping("/list")
    public Map<String, Object> getList() { // Loại bỏ tham số page và size
        log.info("Get category list (no pagination)");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "category list");
        result.put("data", categoryService.findAll()); // Gọi phương thức findAll mới
        return result;
    }

    @Operation(summary = "Get category detail", description = "API retrieve category detail by ID from database")
    @GetMapping("/{categoryId}")
    public Map<String, Object> getCategoryDetail(@PathVariable @Min(value = 1, message = "categoryId must be equals or greater than 1") Long categoryId) {
        log.info("Get category detail by ID: {}", categoryId);
        CategoryResponse categoryDetail = categoryService.findById(categoryId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "category");
        result.put("data", categoryDetail);
        return result;
    }

    @Operation(summary = "Create Category", description = "API add new category to database")
    @PostMapping("/add")
    public ResponseEntity<Object> createCategory(@RequestBody @Valid CategoryCreationRequest request) {
        log.info("Create Category: {}", request);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Category created successfully");
        result.put("data", categoryService.save(request));
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Category", description = "API update category to database")
    @PutMapping("/upd")
    public Map<String, Object> updateCategory(@RequestBody @Valid CategoryUpdateRequest request) {
        log.info("Updating category: {}", request);
        categoryService.update(request);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "Category updated successfully");
        result.put("data", "");
        return result;
    }

    @Operation(summary = "Delete category", description = "API delete category from database")
    @DeleteMapping("/del/{categoryId}")
    public Map<String, Object> deleteCategory(@PathVariable @Min(value = 1, message = "categoryId must be equals or greater than 1") Long categoryId) {
        log.info("Deleting category: {}", categoryId);
        categoryService.delete(categoryId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.RESET_CONTENT.value());
        result.put("message", "Category deleted successfully");
        result.put("data", "");
        return result;
    }
}