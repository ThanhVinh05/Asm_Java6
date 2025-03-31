package com.poly.service;

import com.poly.controller.request.CategoryCreationRequest;
import com.poly.controller.request.CategoryUpdateRequest;
import com.poly.controller.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> findAll(); // Thay đổi kiểu trả về
    CategoryResponse findById(Long id);
    long save(CategoryCreationRequest req);
    void update(CategoryUpdateRequest req);
    void delete(Long id);
}