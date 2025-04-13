package com.poly.service.impl;

import com.poly.common.Status;
import com.poly.controller.request.CategoryCreationRequest;
import com.poly.controller.request.CategoryUpdateRequest;
import com.poly.controller.response.CategoryResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.CategoryEntity;
import com.poly.repository.CategoryRepository;
import com.poly.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "CATEGORY-SERVICE")
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryResponse> findAll() { // Thay đổi kiểu trả về
        log.info("findAll categories (no pagination)");


        List<CategoryEntity> categoryEntities = categoryRepository.findAllActive();

        return categoryEntities.stream()
                .map(entity -> CategoryResponse.builder()
                        .id(entity.getId())
                        .categoryName(entity.getCategoryName())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse findById(Long id) {
        log.info("Find category by id: {}", id);
        CategoryEntity categoryEntity = getCategoryEntity(id);
        return CategoryResponse.builder()
                .id(categoryEntity.getId())
                .categoryName(categoryEntity.getCategoryName())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long save(CategoryCreationRequest req) {
        log.info("Saving category: {}", req);

        CategoryEntity category = new CategoryEntity();
        category.setCategoryName(req.getCategoryName());
        category.setStatus(Status.ACTIVE);

        categoryRepository.save(category);
        log.info("Saved category: {}", category);
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CategoryUpdateRequest req) {
        log.info("Updating category: {}", req);

        CategoryEntity category = getCategoryEntity(req.getId());
        category.setCategoryName(req.getCategoryName());

        categoryRepository.save(category);
        log.info("Updated category: {}", category);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting category: {}", id);

        CategoryEntity category = getCategoryEntity(id);
        category.setStatus(Status.INACTIVE);

        categoryRepository.save(category);
        log.info("Deleted category id: {}", id);
    }

    private CategoryEntity getCategoryEntity(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }
}