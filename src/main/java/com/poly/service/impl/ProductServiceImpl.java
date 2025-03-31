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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
    public ProductPageResponse findAll(String keyword, String sort, int page, int size) {
        log.info("findAll start, page={}, size={}", page, size);

        Sort.Order order = new Sort.Order(Sort.Direction.ASC, "id");
        if (StringUtils.hasLength(sort)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sort);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    order = new Sort.Order(Sort.Direction.ASC, columnName);
                } else {
                    order = new Sort.Order(Sort.Direction.DESC, columnName);
                }
            }
        }

        int pageNo = 0;
        if (page > 0) {
            pageNo = page - 1;
        }

        Pageable pageable = PageRequest.of(pageNo, size, Sort.by(order));

        Page<ProductEntity> entityPage;

        if (StringUtils.hasLength(keyword)) {
            keyword = "%" + keyword.toLowerCase() + "%";
            entityPage = productRepository.searchByKeyword(keyword, pageable);
        } else {
            entityPage = productRepository.findAllActive(pageable); //Sửa tại đây.
        }

        log.info("Pageable: {}", pageable);
        log.info("EntityPage: {}", entityPage);

        return getProductPageResponse(page, size, entityPage);
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