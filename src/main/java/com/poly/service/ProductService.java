package com.poly.service;

import com.poly.controller.request.ProductCreationRequest;
import com.poly.controller.request.ProductUpdateRequest;
import com.poly.controller.response.ProductPageResponse;
import com.poly.controller.response.ProductResponse;

import java.math.BigDecimal;

public interface ProductService {

    ProductPageResponse findAll(String keyword, String sort, int page, int size, Long categoryId, BigDecimal minPrice, BigDecimal maxPrice);

    ProductResponse findById(Long id);

    long save(ProductCreationRequest req);

    void update(ProductUpdateRequest req);

    void delete(Long id);

}
