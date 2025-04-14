package com.poly.service.impl;

import com.poly.common.Status;
import com.poly.controller.request.ProductCreationRequest;
import com.poly.controller.request.ProductUpdateRequest;
import com.poly.controller.response.ProductPageResponse;
import com.poly.controller.response.ProductResponse;
import com.poly.exception.ResourceNotFoundException;
import com.poly.model.ProductEntity;
import com.poly.repository.ProductRepository;
import com.poly.service.ProductService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j(topic = "PRODUCT-SERVICE")
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    public ProductPageResponse findAll(String keyword, String sort, int page, int size, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("findAll products with filters: keyword={}, sort={}, page={}, size={}, categoryId={}, minPrice={}, maxPrice={}",
                keyword, sort, page, size, categoryId, minPrice, maxPrice);

        // 1. Xử lý Sắp xếp (Sort)
        Sort sorting = buildSort(sort);

        // 2. Xử lý Phân trang (Pageable)
        int pageNo = Math.max(0, page - 1); // Đảm bảo page không âm
        Pageable pageable = PageRequest.of(pageNo, size, sorting);

        // 3. Xây dựng Điều kiện lọc (Specification)
        Specification<ProductEntity> specification = buildSpecification(keyword, categoryId, minPrice, maxPrice);

        // 4. Gọi Repository với Specification và Pageable
        Page<ProductEntity> entityPage = productRepository.findAll(specification, pageable);

        log.info("Found {} products matching criteria.", entityPage.getTotalElements());

        // 5. Chuyển đổi sang Response DTO
        return getProductPageResponse(page, size, entityPage);
    }

    // Hàm xây dựng đối tượng Sort
    private Sort buildSort(String sortParam) {
        if (StringUtils.hasLength(sortParam)) {
            switch (sortParam) {
                case "price_low":
                    return Sort.by(Sort.Direction.ASC, "productPrice");
                case "price_high":
                    return Sort.by(Sort.Direction.DESC, "productPrice");
                case "newest":
                    return Sort.by(Sort.Direction.DESC, "createdAt");
                case "featured": // Featured có thể là mặc định hoặc một logic khác
                default:
                    return Sort.by(Sort.Direction.ASC, "id"); // Mặc định theo ID
            }
        }
        return Sort.by(Sort.Direction.ASC, "id"); // Mặc định nếu không có sort
    }

    // Hàm xây dựng đối tượng Specification
    private Specification<ProductEntity> buildSpecification(String keyword, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Luôn lọc theo status ACTIVE
            predicates.add(criteriaBuilder.equal(root.get("status"), Status.ACTIVE));

            // Lọc theo keyword (tìm trong tên sản phẩm)
            if (StringUtils.hasLength(keyword)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%" + keyword.toLowerCase() + "%"));
            }

            // Lọc theo categoryId
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("categoryId"), categoryId));
            }

            // Lọc theo minPrice
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("productPrice"), minPrice));
            }

            // Lọc theo maxPrice
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("productPrice"), maxPrice));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }


    @Override
    public ProductResponse findById(Long id) {
        log.info("Find product by id: {}", id);

        ProductEntity productEntity = getProductEntity(id);

        return ProductResponse.builder()
                .id(id)
                .productName(productEntity.getProductName())
                .image(productEntity.getImage())
                .description(productEntity.getDescription())
                .productPrice(productEntity.getProductPrice())
                .stockQuantity(productEntity.getStockQuantity())
                .categoryId(productEntity.getCategoryId())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public long save(ProductCreationRequest req) {
        log.info("Saving product: {}", req);

        ProductEntity product = new ProductEntity();
        product.setProductName(req.getProductName());
        product.setImage(req.getImage());
        product.setDescription(req.getDescription());
        product.setProductPrice(req.getProductPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setCategoryId(req.getCategoryId());

        product.setStatus(Status.ACTIVE); // Thêm dòng này

        productRepository.save(product);
        log.info("Saved product: {}", product);

        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProductUpdateRequest req) {
        log.info("Updating product: {}", req);

        ProductEntity product = getProductEntity(req.getId());
        product.setProductName(req.getProductName());
        product.setImage(req.getImage());
        product.setDescription(req.getDescription());
        product.setProductPrice(req.getProductPrice());
        product.setStockQuantity(req.getStockQuantity());
        product.setCategoryId(req.getCategoryId());

        productRepository.save(product);
        log.info("Updated product: {}", product);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting product: {}", id);

        ProductEntity product = getProductEntity(id);
        product.setStatus(Status.INACTIVE);

        productRepository.save(product);
        log.info("Deleted product id: {}", id);
    }

    private ProductEntity getProductEntity(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private ProductPageResponse getProductPageResponse(int page, int size, Page<ProductEntity> productEntities) {
        log.info("Convert Product Entity Page");

        List<ProductResponse> productList = productEntities.stream().map(entity -> ProductResponse.builder()
                .id(entity.getId())
                .productName(entity.getProductName())
                .image(entity.getImage())
                .description(entity.getDescription())
                .productPrice(entity.getProductPrice())
                .stockQuantity(entity.getStockQuantity())
                .categoryId(entity.getCategoryId())
                .build()).collect(Collectors.toList());

        ProductPageResponse response = new ProductPageResponse();
        response.setPageNumber(page);
        response.setPageSize(size);
        response.setTotalElements(productEntities.getTotalElements());
        response.setTotalPages(productEntities.getTotalPages());
        response.setProducts(productList);

        return response;
    }
}