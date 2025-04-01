package com.poly.controller;

import com.poly.controller.request.ProductCreationRequest;
import com.poly.controller.request.ProductUpdateRequest;
import com.poly.controller.response.ProductResponse;
import com.poly.service.ProductService;
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
@RequestMapping("/product")
@Tag(name = "Product Controller")
@Slf4j(topic = "PRODUCT-CONTROLLER")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Get product list", description = "API retrieve product from database")
    @GetMapping("/list")
    public Map<String, Object> getList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long categoryId) { // Thêm categoryId
        log.info("Get product list, page={}, size={}, categoryId={}", page, size, categoryId); // Thêm log

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "product list");
        result.put("data", productService.findAll(keyword, sort, page, size, categoryId)); // Truyền categoryId
        return result;
    }

    @Operation(summary = "Get product detail", description = "API retrieve product detail by ID from database")
    @GetMapping("/{productId}")
    public Map<String, Object> getProductDetail(@PathVariable @Min(value = 1, message = "productId must be equals or greater than 1") Long productId) {
        log.info("Get product detail by ID: {}", productId);

        ProductResponse productDetail = productService.findById(productId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.OK.value());
        result.put("message", "product");
        result.put("data", productDetail);

        return result;
    }

    @Operation(summary = "Create Product", description = "API add new product to database")
    @PostMapping("/add")
    public ResponseEntity<Object> createProduct(@RequestBody @Valid ProductCreationRequest request) {
        log.info("Create Product: {}", request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.CREATED.value());
        result.put("message", "Product created successfully");
        result.put("data", productService.save(request));

        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Product", description = "API update product to database")
    @PutMapping("/upd")
    public Map<String, Object> updateProduct(@RequestBody @Valid ProductUpdateRequest request) {
        log.info("Updating product: {}", request);

        productService.update(request);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.ACCEPTED.value());
        result.put("message", "Product updated successfully");
        result.put("data", "");

        return result;
    }

    @Operation(summary = "Delete product", description = "API delete product from database")
    @DeleteMapping("/del/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable @Min(value = 1, message = "productId must be equals or greater than 1") Long productId) {
        log.info("Deleting product: {}", productId);

        productService.delete(productId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", HttpStatus.RESET_CONTENT.value());
        result.put("message", "Product deleted successfully");
        result.put("data", "");

        return result;
    }
}
